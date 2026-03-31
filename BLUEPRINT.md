# TrufflΣ — Master Project Blueprint

> **Scope**: Native Android (Kotlin / Jetpack Compose) wallet for the Ergo blockchain.
> Supports token swaps via Spectrum DEX AMM pools, stablecoin minting (USE, DexyGold, SigmaUSD/SigRSV), an ecosystem dashboard, ErgoPay (EIP-20), and biometric wallet encryption.

---

## 1 · Directory Tree

```
Truffle/
├── app/
│   ├── build.gradle.kts                    # Gradle build config (deps, signing, etc.)
│   └── src/
│       ├── dev/java/.../
│       │   ├── protocol/ProtocolConfig.kt  # Dev-only override for NetworkConfig
│       │   └── wallet/jni/WalletDev.kt     # Dev stub for JNI signing calls
│       └── main/
│           ├── AndroidManifest.xml
│           ├── assets/
│           │   ├── bip39_english.txt        # BIP-39 wordlist
│           │   ├── libergowalletlibjni.so   # Native ergo-lib JNI binary
│           │   └── token_logos/             # ~100 PNG/WebP token icons
│           ├── java/com/piggytrade/piggytrade/
│           │   ├── MainActivity.kt          # Single-Activity host, Compose navigation
│           │   ├── TruffleApplication.kt    # Application subclass, singleton bootstrap
│           │   ├── blockchain/              # AMM math, tx building, ErgoPay, signing
│           │   │   ├── Amm.kt
│           │   │   ├── ErgoPayReceiver.kt
│           │   │   ├── ErgoSigner.kt
│           │   │   ├── SendTxBuilder.kt
│           │   │   ├── TradeMapper.kt
│           │   │   ├── Trader.kt
│           │   │   └── TxBuilder.kt
│           │   ├── crypto/                  # Mnemonic encryption, biometrics
│           │   │   ├── BiometricHelper.kt
│           │   │   ├── DeviceEncryption.kt
│           │   │   ├── MnemonicEncryption.kt
│           │   │   ├── MnemonicValidator.kt
│           │   │   └── WalletManager.kt
│           │   ├── data/                    # Persistence, caching, session state
│           │   │   ├── OraclePriceStore.kt
│           │   │   ├── PreferenceManager.kt
│           │   │   ├── SessionManager.kt
│           │   │   └── TokenRepository.kt
│           │   ├── network/                 # Ergo node HTTP layer
│           │   │   ├── NodeClient.kt
│           │   │   ├── NodeManager.kt
│           │   │   └── NodePool.kt
│           │   ├── protocol/
│           │   │   └── NetworkConfig.kt     # Hardcoded node URLs, token IDs, protocol addresses
│           │   ├── stablecoin/              # Protocol-agnostic mint/redeem framework
│           │   │   ├── StablecoinProtocol.kt    # Interface + shared DTOs
│           │   │   ├── StablecoinRegistry.kt    # Central protocol catalog
│           │   │   ├── VlqCodec.kt              # VLQ+ZigZag register codec
│           │   │   ├── use/                     # USE stablecoin
│           │   │   │   ├── UseConfig.kt
│           │   │   │   ├── UseFreemintProtocol.kt
│           │   │   │   └── UseArbmintProtocol.kt
│           │   │   ├── sigmausd/                # AgeUSD (SigmaUSD + SigRSV)
│           │   │   │   ├── SigmaUsdConfig.kt
│           │   │   │   ├── SigmaUsdBank.kt
│           │   │   │   ├── SigmaUsdMintProtocol.kt
│           │   │   │   └── SigmaRsvMintProtocol.kt
│           │   │   └── dexygold/                # DexyGold stablecoin
│           │   │       ├── DexyGoldConfig.kt
│           │   │       ├── DexyGoldFreemintProtocol.kt
│           │   │       └── DexyGoldArbmintProtocol.kt
│           │   └── ui/                      # Jetpack Compose screens & ViewModels
│           │       ├── bank/
│           │       │   ├── BankScreen.kt
│           │       │   └── BankComponents.kt
│           │       ├── common/
│           │       │   ├── Views.kt
│           │       │   └── TransactionDetailsView.kt
│           │       ├── home/
│           │       │   ├── MainScreen.kt
│           │       │   ├── HomeComponents.kt
│           │       │   ├── MainPopups.kt
│           │       │   └── MarketSyncDialog.kt
│           │       ├── market/
│           │       │   └── MarketViewModel.kt
│           │       ├── portfolio/
│           │       │   └── EcosystemScreen.kt
│           │       ├── settings/
│           │       │   ├── SettingsScreen.kt
│           │       │   └── AddNodeScreen.kt
│           │       ├── swap/                # (SwapScreen etc. — referenced in imports)
│           │       ├── wallet/              # (WalletScreen etc. — referenced in imports)
│           │       └── theme/               # Material 3 colour/typography tokens
│           └── res/                         # XML resources (layouts, strings, icons)
├── sigma-rust/
│   └── bindings/ergo-lib-jni/              # Rust JNI bridge for native tx signing
├── branding/                                # App icons and marketing assets
├── dev_tools/                               # Utility scripts (Python bots, staking tools)
├── CHANGELOG.md
├── Security_Audit.md
└── README.md
```

---

## 2 · System Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                          USER (Android UI)                          │
│  MainScreen ─┬─ HomeTab (wallet balances, price chart, favorites)   │
│              ├─ SwapTab (DEX token swap)                           │
│              ├─ BankTab (stablecoin mint/redeem)                   │
│              ├─ EcosystemTab (TVL, activity, token charts)         │
│              └─ Settings (node mgmt, wallet import)                │
└──────┬──────────────────────┬───────────────┬──────────────────────┘
       │                      │               │
       ▼                      ▼               ▼
  MarketViewModel      BankScreen        SwapScreen
  (price data,        (protocol UI)    (pool selection)
   ecosystem)               │               │
       │                     │               │
       ▼                     ▼               ▼
  SessionManager ──────► shared singleton services ◄────────────────
  ├─ PreferenceManager   (wallets, nodes, settings, trades)
  ├─ TokenRepository     (whitelisted tokens, pool metadata, UTXO fetch)
  ├─ OraclePriceStore    (oracle + DEX price history, disk cache)
  ├─ NodePool            (multi-node HA pool, health probe)
  └─ NodeManager         (selected node, NodeClient lifecycle)
              │
              ▼
       NodeClient (Retrofit HTTP → Ergo node REST API)
              │
   ┌──────────┼───────────────┐
   │          │               │
   ▼          ▼               ▼
 UTXO      Oracle         Mempool
 queries   price feed     tx state
   │          │
   ▼          ▼
Trader / StablecoinProtocol / SendTxBuilder
   │  (quote + build unsigned tx)
   ▼
ErgoSigner
   ├─ signTransaction()  → ergo-lib-jni (native Rust)  → signed JSON
   ├─ reduceTxForErgopay() → ReducedTransaction → ergopay:<base64>
   └─ toUnsignedJson()   → legacy ErgoPay fallback
   │
   ▼
NodeClient.submitTx()  →  ON-CHAIN
```

### Data Flow Summary

1. **Startup**: `TruffleApplication.onCreate()` registers all stablecoin protocols via `StablecoinRegistry.initialize()`. `SessionManager` is lazily created, which bootstraps `PreferenceManager`, `TokenRepository`, `OraclePriceStore`, `NodePool`, and `NodeManager`.

2. **Node selection**: `NodeManager` reads persisted nodes + hardcoded `NetworkConfig.NODES`, builds the display list, and creates a `NodeClient` from the selected URL. `NodePool` probes all 22 nodes in parallel on first use to build a healthy rotation pool.

3. **Wallet load**: The UI reads the selected wallet from `PreferenceManager`, decrypts the mnemonic (`MnemonicEncryption` or `WalletManager`), derives addresses through `ergo-lib-jni`, then queries UTXOs via `NodeClient.getMyAssetsMulti()`.

4. **Market data**: `OraclePriceStore.syncAll()` fetches oracle box history from the node, computes per-token price points/volume, and caches to JSON files on disk. `MarketViewModel` exposes this via `StateFlow` + coordinates chart range switches.

5. **Token swap**: `TradeMapper` resolves pool routes (ERG↔Token or Token↔Token). `Trader.getQuote()` fetches the live pool box, calls `Amm.buyToken/sellToken/tokenForToken` for the AMM calculation, then `Trader.buildSwapTransaction()` constructs the full txDict via `TxBuilder.buildSwapTx()`.

6. **Stablecoin mint**: The selected `StablecoinProtocol` (e.g. `UseFreemintProtocol`) fetches on-chain protocol boxes, checks eligibility/capacity, computes quote, and builds the unsigned tx dict. `ErgoSigner.signTransaction()` or `reduceTxForErgopay()` finalises and the tx is submitted.

7. **ErgoPay**: `ErgoPayReceiver` supports three flows: static inline base64, dynamic URL fetch, and address-substitution connect. The returned `ReducedTransaction` is handed to the user's external signer.

---

## 3 · Service / Class Catalog (with Line Maps)

> **Line maps** let agents jump directly to a function with `view_file(StartLine=X, EndLine=Y)` instead of reading the whole file.

### 3.1 — `blockchain/Trader.kt` (439 lines)

> End-to-end swap orchestrator: pool lookup → quote → UTXO selection → transaction build.

| Lines | Function | What it does |
|-------|----------|--------------|
| 16–42 | `getPoolConfig(poolKey)` | Fetches pool NFT box from node, extracts ERG/token balances + metadata. |
| 44–154 | `getQuote(poolKey, amount, ...)` | Calculates expected output, price, decimals for a proposed swap. |
| 156–159 | `getBal(box, tid)` | Extracts a token's balance from a UTXO box map. |
| 161–163 | `formatReadable(value, dec)` | Formats raw token amount to human-readable with decimals. |
| **165–367** | **`buildSwapTransaction(...)`** | 🔥 Full tx construction: pool + user UTXOs, fees, calls `TxBuilder`. |
| 369–437 | `selectMinimumBoxes(...)` | Smart multi-address box selection to minimise input count. |

**Dependencies**: `NodeClient`, `Amm`, `TxBuilder`, `ErgoSigner`, `TokenRepository`.

---

### 3.2 — `blockchain/ErgoSigner.kt` (381 lines)

> Transaction signing and ErgoPay URL construction via native `ergo-lib-jni`.

| Lines | Function | What it does |
|-------|----------|--------------|
| 21–53 | `Base58Decoder.decode(input)` | Base58 → byte array. |
| 55–68 | `Base58Decoder.addressToErgoTreeHex(address)` | Address → ErgoTree hex. |
| 73–75 | `calculateAppFee(n, mode)` | Computes app service fee for swaps. |
| 77–79 | `calculateAppFeeStablecoin(n)` | App fee for stablecoin operations. |
| 81–87 | `parseAmount(a)` | Safely coerces heterogeneous numeric types to `Long`. |
| 89–147 | `sanitizeBox(box)` | Normalises a raw UTXO map for the signing library. |
| **154–227** | **`reduceTxForErgopay(...)`** | Builds a `ReducedTransaction` via native JNI → `ergopay:<base64>` URL. |
| 229–250 | `toErgoPayUrl(base64)` | Formats Base64 payload into URL-safe ErgoPay URI. |
| **252–293** | **`signTransaction(...)`** | Signs tx in-app using mnemonic via native JNI. |
| 295–303 | `reduceTxForErgopayLegacy(...)` | Legacy unsigned JSON ErgoPay fallback. |
| 305–379 | `toUnsignedJson(txDict, senderAddress)` | Serialises tx dict into node-compatible unsigned TX JSON. |

**Dependencies**: `ergo-lib-jni` (native), `ProtocolConfig`, `NodeClient`.

---

### 3.3 — `blockchain/TxBuilder.kt` (178 lines)

> Raw transaction request dict builder (outputs, change, fees) consumed by `ErgoSigner`.

| Lines | Function | What it does |
|-------|----------|--------------|
| 21–27 | `parseBigInt(a)` | Safe BigInteger parser. |
| **29–134** | **`buildSwapTx(...)`** | Assembles pool output, fee output, and change boxes. |
| 136–176 | `splitChangeTokens(...)` | Splits >100 change tokens across multiple boxes. |

---

### 3.4 — `blockchain/SendTxBuilder.kt` (282 lines)

> Multi-recipient ERG + token send transactions.

| Lines | Function | What it does |
|-------|----------|--------------|
| 25–34 | `SendRecipient` / `TokenAmount` | Data classes for send inputs. |
| **36–188** | **`buildSendTx(...)`** | Constructs send txDict with optimal UTXO selection. |
| 190–232 | `splitTokensIntoBoxes(...)` | Splits >100 tokens across multiple output boxes. |
| 234–280 | `selectBoxes(allBoxes, requiredErg, requiredTokens)` | Greedy UTXO selection. |

---

### 3.5 — `blockchain/Amm.kt` (pure math, no network)

| Function | What it does |
|----------|--------------|
| `buyToken(amount, ergPool, tokenAmountFull)` | ERG → token via constant-product. |
| `sellToken(tokensToSell, poolNanoerg, tokenBalance)` | Token → ERG. |
| `tokenForToken(tokensToSell, txInBalance, txOutBalance, feePercentage)` | Token-to-Token swap. |

---

### 3.6 — `blockchain/TradeMapper.kt`

| Function | What it does |
|----------|--------------|
| `allAssets()` | All unique tradeable asset names. |
| `resolve(fromAsset, toAsset)` | Resolves pool key, order type, pool type. |
| `toAssetsFor(fromAsset)` | Lists available swap targets. |

---

### 3.7 — `network/NodeClient.kt` (337 lines)

> Retrofit HTTP client for the Ergo node REST API.

| Lines | What |
|-------|------|
| 17–112 | `ErgoNodeApi` — Retrofit interface: all endpoint definitions. |
| 114–337 | `NodeClient` class — wrapper with helpers. |
| 144–147 | `getHeight()` — Current blockchain height. |
| 149–173 | `getMyAssets(address, checkMempool)` — Paginated UTXO fetch for one address. |
| 181–199 | `getMyAssetsMulti(addresses, checkMempool)` — Multi-address balances. |
| 260–298 | `getPoolBox(tokenId, ..., expectedAddress)` — Fetch contract box by NFT. |
| 311–313 | `submitTx(signedTx)` — Submit signed transaction. |

---

### 3.8 — `network/NodePool.kt` (190 lines)

> High-availability read-only node pool with health probing.

| Lines | Function | What it does |
|-------|----------|--------------|
| 54–101 | `probeAll(timeoutMs, onResult)` | Probes all 22 nodes in parallel (liveness + indexer check). |
| 109–114 | `next()` | Round-robin next live client. |
| 122–127 | `withRetry(maxRetries, block)` | Execute with automatic retry across nodes. |
| 129–176 | `withRetryTracked(...)` | Same + fires `onTrying` callback for UI indicators. |

---

### 3.9 — `network/NodeManager.kt` (158 lines)

> Owns node configuration state and active `NodeClient` lifecycle.

| Lines | Function | What it does |
|-------|----------|--------------|
| 63–114 | `initializeNodeClient(allowHttp, onClientReady, onFailed)` | Builds `NodeClient` from selected node. |
| 116–118 | `setSelectedNodeIndex(index)` | Updates selection. |
| 120–148 | `deleteSelectedNode()` | Removes node + rebuilds list. |
| 150–156 | `reloadNodes()` | Re-reads from preferences. |

---

### 3.10 — `data/TokenRepository.kt` (896 lines)

> Token metadata catalog: whitelisted tokens, pool NFTs, sync, name resolution.

| Lines | Function | What it does |
|-------|----------|--------------|
| 27–32 | `loadSystemWhitelistNameMap()` | Built-in tokenId → display name map. |
| 34–57 | `loadSystemWhitelistPids()` / `loadCustomWhitelistPids()` | Pool NFT sets. |
| 81–121 | `getVerificationStatus(tokenKey)` | 0=Official, 1=UserAdded, 2=Unverified. |
| **142–452** | **`syncTokensWithBlockchain(...)`** | 🔥 Multi-pass blockchain scanner for all pool NFTs. |
| 458–466 | `normalizeTokenName(name)` | Strips prefixes, uppercases. |
| 476–583 | `loadCombinedTokens()` | Merges system + synced + custom tokens. |
| 614–617 | `getTokenName(tokenId)` | Token ID → display name. |
| 648–660 | `getTokenDecimals(tokenId)` | Returns decimal places. |
| 662–678 | `getPoolNftForToken(tokenName)` / `getTokenIdForName(tokenName)` | Reverse lookups. |
| 680–698 | `getWhitelistedTokensWithPools()` | `List<Triple<name, poolNft, decimals>>` for sync. |
| 700–716 | `getTokenNamesWithPools()` | Sorted list of ERG-pool token names. |
| 730–771 | `fetchAllBoxesForAddress(...)` | Paginated UTXO fetch for a single address. |

---

### 3.11 — `data/OraclePriceStore.kt` (988 lines)

> On-chain oracle and DEX pool price history with local JSON file caching.

| Lines | Function | What it does |
|-------|----------|--------------|
| 40–60 | Data classes: `PricePoint`, `VolumePoint`, `PriceHistory`, `TokenMarketData` | Core data models. |
| 102–110 | `loadAll()` | Loads all cached price history from disk. |
| 112–136 | `resilientFetch(nodePool, label, block)` | Fault-tolerant multi-node API call. |
| 138–207 | `syncAll(nodePool)` | Master sync: USE oracle, SigUSD oracle, SigUSD DEX pool. |
| 259–353 | `syncOracle(nodePool, nft, ...)` | Syncs a single oracle NFT's price history. |
| 355–439 | `syncDexPool(nodePool, poolNft, ...)` | Syncs SigUSD DEX pool price. |
| **441–588** | **`syncTokenDex(nodePool, tokenName, ...)`** | 🔥 Walks DEX pool box chain for any token → price + volume. |
| 590–602 | `getTokenVolume(tokenName)` | `Pair(vol24h, vol7d)` in ERG. |
| 604–636 | `computeMarketData(tokenName)` | Price, 24h change, volume for a token. |
| 638–652 | `rebuildMarketDataFromCache(tokens)` | Restores market data from disk (no network). |
| **654–772** | **`syncAllTokens(nodePool, tokens)`** | Parallel multi-node sync of all whitelisted tokens. |
| 786–850 | `getHistory(source, range)` | Sampled price history for chart display. |
| 863–909 | `getTokenHistory(tokenName, range)` | Token price history (in ERG). |
| 916–933 | `clearAll()` | Deletes all cache files and resets state. |

---

### 3.12 — `data/PreferenceManager.kt`

> Encrypted SharedPreferences wrapper for all persisted user data.

| Function | What it does |
|----------|--------------|
| `saveWallets(wallets)` / `loadWallets()` | Persist/load wallet map. |
| `saveNodes(nodes)` / `loadNodes()` | Persist/load custom nodes. |
| `saveFavorites(favorites)` / `loadFavorites(default)` | Persist/load favorite tokens. |
| `saveSettings(settings)` / `loadSettings()` | Persist/load app settings. |
| `saveTrades(trades)` / `loadTrades()` | Persist/load trade history. |
| `saveWalletAddressConfig(...)` / `loadWalletAddressConfig(...)` | Per-wallet multi-address config. |
| `saveExplorerAddresses(...)` / `loadExplorerAddresses()` | Watch-only explorer addresses. |

---

### 3.13 — `data/SessionManager.kt`

> Application-scoped DI container — single source of truth for all shared services.

Fields: `preferenceManager`, `tokenRepository`, `oraclePriceStore`, `nodePool`, `nodeManager`, `nodeClient`.

---

### 3.14 — `ui/market/MarketViewModel.kt` (960 lines)

> ViewModel for market/price data, ecosystem feed, pool trades.

| Lines | Item | What it does |
|-------|------|--------------|
| 28–64 | `MarketState` data class | All observable state for the ecosystem/market UI. |
| 66–91 | Class fields | `ecosystemPage`, `poolTradesPage`, `currentPoolTradesToken`, caches. |
| 92–112 | `fetchErgPrice(range)` | Loads oracle price history for chart display. |
| 114–144 | `syncOraclePrices()` | Background oracle + token market data sync. |
| 148–180 | `startMarketSync()` | Syncs all whitelisted tokens' price + volume. |
| 221–230 | `setChartRange(range)` | Switches chart time range. |
| 246–280 | `selectChartToken(tokenName?)` | Selects a token for the price chart + triggers trade fetch. |
| **286–386** | **`fetchPoolTrades(tokenName)`** | Fetches pool box history → parses into trade list. |
| 390–513 | `fetchTokenUsdValues(...)` / `fetchTokenUsdValuesInternal(...)` | Computes USD values for wallet tokens via DEX pools. |
| 519–546 | `fetchEcosystemData(forceRefresh)` | Fetches TVL + activity (uses `fetchEcosystemActivityInternal`). |
| 548–571 | `fetchMoreEcosystemActivity()` | Paginated load-more for ecosystem activity feed. |
| **573–689** | **`fetchMorePoolTrades(tokenName)`** | Paginated load-more for pool trades. |
| 721–754 | `fetchEcosystemTvlInternal(client)` | Fetches ERG locked in DEX/stablecoin contracts. |
| 765–779 | `fetchEcosystemActivityInternal(client, offset)` | Fetches txs from all protocol addresses. |
| **789–939** | **`parseEcosystemTx(...)`** | Parses a raw tx map into `EcosystemTx` (DEX swap detection, LP deposit/withdraw, stablecoin ops). |
| 941–958 | `calculatePriceImpact(...)` | Computes price impact % from pool box deltas. |

---

### 3.15 — `ui/portfolio/EcosystemScreen.kt` (2191 lines)

> Ecosystem dashboard: TVL, activity feed, per-token price charts, pool trades, token holders.

| Lines | Composable / Section | What it shows |
|-------|---------------------|---------------|
| 71–1024 | `EcosystemScreen(...)` | Main screen: tab filters (DEX Swaps, Stable Coins, Latest Trades, Holders, Market), pull-to-refresh. |
| 71–100 | ↳ State setup | `filteredActivity`, filter state, `selectedChartToken`. |
| 100–230 | ↳ Filter chips + tab routing | DEX/Stable/Latest/Holders/Market filter buttons. |
| 230–500 | ↳ Latest Trades LazyColumn | Pool trade rows with infinite scroll + detail dialogs. |
| 500–740 | ↳ Holders tab | Top token holders table with sorting. |
| 740–930 | ↳ Market tab | Token market data table. |
| 930–970 | ↳ Activity feed LazyColumn | Ecosystem activity rows with infinite scroll via `rememberLazyListState`. |
| 1029–1517 | `ErgPriceChartCard(...)` | Price chart with token selector dropdown, multi-series Canvas renderer, range pills. |
| 1519–1542 | `formatCompactPrice(value)` | Compact Y-axis label formatter. |
| 1544–1678 | `TokenPriceChart(...)` | Standalone token price chart Canvas. |
| 1680–1700 | `LegendDot(...)` | Chart legend dot composable. |
| 1702–1799 | `shareChart(...)` / `addWatermarkAndShare(...)` | Screenshot→share workflow. |
| 1820–1871 | `TvlSection(tvl, ergPriceUsd)` | Protocol TVL breakdown cards. |
| 1906–2138 | `EcosystemTxRow(tx, onAddressClick)` | Single activity row with expandable details + explorer links. |
| 2143–2190 | Helper functions | `getEcoIcon`, `getEcoTagColor`, `ecoTimeAgo`, `ecoDate`. |

---

### 3.16 — `ui/swap/SwapViewModel.kt` (3610 lines)

> Primary ViewModel — DEX swap, wallet management, bank (stablecoin), send, address explorer, settings.

| Lines | Item | What it does |
|-------|------|--------------|
| 44–48 | `NodeStatus` | Sealed class: `Trying`, `Connected`, `Failed`. |
| 50–149 | `SwapState` data class | All observable state for the main app. |
| 190–210 | `EcosystemTx` / `PoolTrade` data classes | Shared data models (also used by `MarketViewModel`). |
| 278–427 | `SwapViewModel` init + node setup | Loads wallets/favorites/settings, probes nodes, fetches balances. |
| 499–538 | `initializeNodeClient()` | Builds `NodeClient` from selected node. |
| 573–588 | `readNode(block)` | Executes read-only call through `NodePool` with retry + UI tracking. |
| 616–674 | Balance formatting | `formatBalance`, `updateBalances`, `getUserBalance`. |
| 678–784 | UI state setters (DEX) | `setFromAsset`, `setToAsset`, `setFromAmount`, `swapDirection`, node/settings setters. |
| 803–875 | `fetchWalletBalances(force)` | Multi-address UTXO fetch + balance aggregation. |
| 877–1016 | `saveWallet(...)` | Mnemonic encryption + address derivation + persistence. |
| 1113–1281 | Multi-address management | `toggleAddress`, `setChangeAddress`, `deriveMoreAddresses`, `removeAddress`. |
| 1297–1401 | Bank (stablecoin) operations | `setBankProtocol`, `fetchBankQuote`, `refreshBankEligibility`. |
| 1403–1599 | Bank tx building | `buildMintTransaction`, `buildRedeemTransaction`. |
| 1617+ | DEX swap operations | `fetchQuote`, `buildSwapTransaction`, `signAndSubmit*`. |
| 2200+ | Send operations | `buildSendTx`, send review/sign/submit flow. |
| 2600+ | Transaction history | `fetchTransactionHistory`, `fetchMoreHistory`, address explorer. |
| 3000+ | Token holders | `fetchTopHolders`, `isMintInfoExpandable`, etc. |
| 3400+ | Pool mappings | `loadPoolMappings`, `syncTokenList`, `getVerificationStatus`. |

---

### 3.17 — `stablecoin/` (Protocol Implementations)

All implement `StablecoinProtocol` interface: `checkEligibility → getQuote → buildTransaction → postProcessUnsignedTx`.

| File | Protocol | Key internal logic |
|------|----------|-------------------|
| `UseFreemintProtocol.kt` | USE Freemint | 5-box tx, cycle-aware capacity, VLQ registers, buyback extension injection. |
| `UseArbmintProtocol.kt` | USE Arbmint | Arbitrage minting with DEX pool price comparison. |
| `DexyGoldFreemintProtocol.kt` | DexyGold Freemint | Same pattern as USE but for gold-pegged stablecoin. |
| `DexyGoldArbmintProtocol.kt` | DexyGold Arbmint | Gold arbitrage minting. |
| `SigmaUsdMintProtocol.kt` | SigmaUSD Mint/Redeem | AgeUSD bank contract, R4/R5 register updates, receipt box. |
| `SigmaRsvMintProtocol.kt` | SigRSV Mint/Redeem | Reserve coin variant of AgeUSD. |
| `SigmaUsdBank.kt` | AgeUSD Economics | Pure math: reserve ratios, pricing, mint limits. |

---

### 3.18 — `crypto/`

| File | Purpose |
|------|---------|
| `MnemonicEncryption.kt` | SCrypt → AES-CBC + HMAC-SHA256 for mnemonic phrases. |
| `WalletManager.kt` | Legacy Fernet-encrypted wallet data decryption. |
| `BiometricHelper.kt` | Android Keystore AES key + `BiometricPrompt.CryptoObject`. |
| `DeviceEncryption.kt` | AES-GCM on-disk encryption with device-unique Keystore key. |
| `MnemonicValidator.kt` | BIP-39 dictionary + checksum validation. |

---

## 4 · State & Data Models

### Source of Truth

| Data | Source of Truth | Persistence |
|------|----------------|-------------|
| Wallet mnemonics | `PreferenceManager` (EncryptedSharedPreferences) | AES-encrypted on disk |
| Selected node/wallet | `PreferenceManager.selectedNode` / `.selectedWallet` | Encrypted prefs |
| Custom nodes | `PreferenceManager.loadNodes()` merged with `NetworkConfig.NODES` | Encrypted prefs |
| Token metadata & pools | `TokenRepository` (in-memory map + JSON files in app filesDir) | JSON cache on disk |
| Oracle + token price history | `OraclePriceStore` (in-memory `PriceHistory` + JSON files) | JSON cache on disk |
| Active node client | `NodeManager._nodeClient` (`StateFlow<NodeClient?>`) | Memory only |
| Node health status | `NodePool.deadIndices` | Memory only (resets per launch) |
| Wallet balances/UTXOs | `MarketViewModel` / home screen state | Memory only (re-fetched) |
| App settings | `PreferenceManager.loadSettings()` | Encrypted prefs |
| Trade history | `PreferenceManager.loadTrades()` | Encrypted prefs |

### Core Data Models

```
EncryptedMnemonic         { salt: String, token: String }
MnemonicValidationResult  { invalidWordIndices, isValidWordCount, checksumValid }
TradeRoute                { tokenKey, orderType, poolType, pid }
MarketState               { ergPrice, chartData, poolTrades, ecosystemTvl, ecosystemActivity,
                            hasMoreEcosystem, isLoadingMorePoolTrades, hasMorePoolTrades, ... }
SwapState                 { fromAsset, toAsset, walletBalances, nodeStatus, bankState, sendState, ... }
PoolTrade                 { isBuy, ergAmount, tokenAmount, timestamp, txId, traderAddress, priceImpact }
EcosystemTx               { txId, protocol, timestamp, traderAddress, sent, received, priceImpact }
TokenMarketData           { price, change24h, vol24h, vol7d }
StatusField               { label, value, status: NEUTRAL|OK|WARNING|ERROR }
EligibilityResult         { canMint, canRedeem, reason, availableCapacity, statusFields }
MintQuote                 { tokenReceived, amountReceived, tokenDecimals, ergCost, feeBreakdown }
RedeemQuote               { tokenRedeemed, amountRedeemed, tokenDecimals, ergReceived, feeBreakdown }
ErgoPayResult             { reducedTx, message, messageSeverity, address, replyTo }
```

---

## 5 · Logic Hotspots

| # | Function | File & Lines | Complexity |
|---|----------|-------------|------------|
| 1 | `Trader.buildSwapTransaction()` | `blockchain/Trader.kt` L165–367 | Fetches pool + user UTXOs, AMM math, multi-address box selection, fee computation, delegates to `TxBuilder`. |
| 2 | `UseFreemintProtocol.buildTransaction()` | `stablecoin/use/UseFreemintProtocol.kt` L152–310 | 5-box stablecoin TX matching ErgoScript contracts, VLQ registers, cycle capacity, buyback extension. |
| 3 | `TokenRepository.syncTokensWithBlockchain()` | `data/TokenRepository.kt` L142–452 | Multi-pass blockchain scanner, paginated pool NFT queries, name/decimal resolution, dedup + merge. |
| 4 | `OraclePriceStore.syncTokenDex()` | `data/OraclePriceStore.kt` L441–588 | Walks box chain for price + volume reconstruction, incremental sync, multi-node resilience. |
| 5 | `SigmaUsdMintProtocol.buildTransaction()` | `stablecoin/sigmausd/SigmaUsdMintProtocol.kt` L173–351 | AgeUSD bank contract interaction, R4/R5 register updates, receipt box, reserve ratio checks. |

---

*Generated 2026-03-29. Updated with line maps and infinite scroll pagination. This blueprint captures the architecture as of the current `develop` branch.*
