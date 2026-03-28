package com.piggytrade.piggytrade.network

import android.util.Log
import com.piggytrade.piggytrade.protocol.NetworkConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.ConcurrentHashMap

/**
 * Pool of NodeClient instances for distributing read-only API calls across
 * multiple public Ergo nodes. Provides round-robin selection and automatic
 * retry-with-fallback when a node fails.
 *
 * On startup, call [probeAll] to eliminate unreachable nodes for the session.
 * Dead-node state is in-memory only — it resets on every app launch so nodes
 * that recover will be retried next time.
 *
 * Write operations (wallet balance, TX submission) should still use the
 * user's selected primary node, NOT this pool.
 */
class NodePool {

    private val clients: List<NodeClient>
    private val counter = AtomicInteger(0)

    /** Indices of nodes that failed the startup health-check (session-scoped, not persisted). */
    private val deadIndices: MutableSet<Int> = ConcurrentHashMap.newKeySet()

    init {
        clients = NetworkConfig.NODES.values.mapNotNull { config ->
            val url = config["url"] as? String ?: return@mapNotNull null
            try { NodeClient(url) } catch (_: Exception) { null }
        }
    }

    /** Number of available nodes (total, including dead ones). */
    val size: Int get() = clients.size

    /** Number of live (reachable) nodes after probing. */
    val liveSize: Int get() = clients.size - deadIndices.size

    /** Emits the URL of the node currently processing a read query (for UI indicators). */
    val activeNodeUrl = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    /** URLs of nodes currently marked as dead this session. */
    val deadNodeUrls = kotlinx.coroutines.flow.MutableStateFlow<Set<String>>(emptySet())

    /** Whether [probeAll] has been run this session. */
    @Volatile var probed: Boolean = false
        private set

    /**
     * Probe every node in parallel with a two-step check:
     *  1. GET /info — basic liveness
     *  2. GET /blockchain/box/unspent/byTokenId — confirms the blockchain indexer is enabled
     *
     * Nodes failing either step are marked dead for the session.
     * Safe to call once on app startup. Results arrive via [onResult] as each probe finishes.
     */
    suspend fun probeAll(
        timeoutMs: Long = 3000L,
        onResult: ((url: String, alive: Boolean) -> Unit)? = null
    ) = coroutineScope {
        deadIndices.clear()
        val jobs = clients.mapIndexed { idx, client ->
            async {
                activeNodeUrl.value = client.nodeUrl
                val alive = withTimeoutOrNull(timeoutMs) {
                    try {
                        // Step 1: basic liveness
                        client.api.getInfo()

                        // Step 2: confirm blockchain indexer is present.
                        // Use the USE LP NFT — it always has an unspent box on indexed nodes.
                        client.api.getUnspentBoxesByTokenId(
                            tokenId = INDEXER_PROBE_TOKEN,
                            offset = 0,
                            limit = 1,
                            includeUnconfirmed = false
                        )
                        true
                    } catch (_: Exception) { false }
                } ?: false  // null = timed out = treat as dead

                if (!alive) {
                    deadIndices.add(idx)
                    Log.w("NodePool", "Node dead or not indexed: ${client.nodeUrl}")
                } else {
                    Log.d("NodePool", "Node alive + indexed: ${client.nodeUrl}")
                }
                onResult?.invoke(client.nodeUrl, alive)
            }
        }
        jobs.forEach { it.await() }
        activeNodeUrl.value = null
        deadNodeUrls.value = deadIndices.map { clients[it].nodeUrl }.toSet()
        probed = true
        Log.i("NodePool", "Probe complete: ${liveSize}/${size} nodes alive+indexed, dead=$deadIndices")
    }

    /** Indices of currently live nodes (not in deadIndices). Falls back to all if all are dead. */
    private fun liveIndices(): List<Int> {
        val live = clients.indices.filter { it !in deadIndices }
        return live.ifEmpty { clients.indices.toList() } // graceful fallback
    }

    /** Get next live client via round-robin */
    fun next(): NodeClient {
        if (clients.isEmpty()) throw IllegalStateException("No nodes available")
        val live = liveIndices()
        return clients[live[counter.getAndIncrement() % live.size]]
    }

    /** Get a random live client */
    fun random(): NodeClient {
        if (clients.isEmpty()) throw IllegalStateException("No nodes available")
        return clients[liveIndices().random()]
    }

    /**
     * Execute a block with automatic retry across different live nodes.
     * Skips nodes marked dead by [probeAll]. Falls back to all nodes if all are dead.
     */
    suspend fun <T> withRetry(maxRetries: Int = 3, block: suspend (NodeClient) -> T): T =
        withRetryTracked(maxRetries = maxRetries, block = block)

    /**
     * Like [withRetry] but fires [onTrying] with the node URL before each attempt,
     * so the UI can display which node is currently being contacted.
     * Dead nodes (from [probeAll]) are skipped automatically.
     */
    suspend fun <T> withRetryTracked(
        maxRetries: Int = 3,
        onTrying: suspend (url: String) -> Unit = {},
        block: suspend (NodeClient) -> T
    ): T {
        var lastError: Exception? = null
        val live = liveIndices()
        val tried = mutableSetOf<Int>()

        repeat(minOf(maxRetries, live.size)) {
            // Pick an untried live node
            var pick = live[counter.getAndIncrement() % live.size]
            var attempts = 0
            while (pick in tried && attempts < live.size) {
                pick = live[counter.getAndIncrement() % live.size]
                attempts++
            }
            tried.add(pick)

            try {
                activeNodeUrl.value = clients[pick].nodeUrl
                onTrying(clients[pick].nodeUrl)
                val result = block(clients[pick])
                activeNodeUrl.value = null // clear when done
                return result
            } catch (e: Exception) {
                activeNodeUrl.value = null
                lastError = e
                // If this is a TLS/SSL/connection error, permanently mark this node dead
                // for the session so it's never retried even on future calls.
                val msg = e.message?.lowercase() ?: ""
                if (msg.contains("tls") || msg.contains("ssl") || msg.contains("handshake") ||
                    msg.contains("tsl") || msg.contains("certificate") ||
                    msg.contains("connection refused") || msg.contains("failed to connect")) {
                    deadIndices.add(pick)
                    deadNodeUrls.value = deadIndices.map { clients[it].nodeUrl }.toSet()
                    Log.w("NodePool", "Marking node dead (runtime TLS/connect error): ${clients[pick].nodeUrl}")
                }
                // Continue to next node
            }
        }
        throw lastError ?: IllegalStateException("All nodes failed")
    }

    companion object {
        /**
         * A well-known token that always has at least one unspent box on a healthy,
         * fully-indexed Ergo node. Used to confirm the /blockchain/ indexer API is enabled,
         * not just that the node is reachable.
         *
         * This is the USE LP NFT (NetworkConfig.USE_CONFIG["lp_nft"]).
         */
        private const val INDEXER_PROBE_TOKEN =
            "ef461517a55b8bfcd30356f112928f3333b5b50faf472e8374081307a09110cf"
    }
}
