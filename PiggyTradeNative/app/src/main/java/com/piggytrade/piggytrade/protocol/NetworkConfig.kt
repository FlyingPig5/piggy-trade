package com.piggytrade.piggytrade.protocol

object NetworkConfig {
    val NODES = mapOf(
        "Pub1" to mapOf("url" to "https://ergo-node.eutxo.de"),
        "Pub2" to mapOf("url" to "https://ergo1.oette.info"),
        "Pub3" to mapOf("url" to "https://ergo2.oette.info"),
        "Pub4" to mapOf("url" to "https://node.sigmaspace.io"),
        "Pub5" to mapOf("url" to "http://128.253.41.101:9053"),
        "Pub6" to mapOf("url" to "http://128.253.41.102:9053"),
        "Pub7" to mapOf("url" to "http://213.239.193.208:9053")
    )

    const val SPECTRUM_ADDRESS = "5vSUZRZbdVbnk4sJWjg2uhL94VZWRg4iatK9VgMChufzUgdihgvhR8yWSUEJKszzV7Vmi6K8hCyKTNhUaiP8p5ko6YEU9yfHpjVuXdQ4i5p4cRCzch6ZiqWrNukYjv7Vs5jvBwqg5hcEJ8u1eerr537YLWUoxxi1M4vQxuaCihzPKMt8NDXP4WcbN6mfNxxLZeGBvsHVvVmina5THaECosCWozKJFBnscjhpr3AJsdaL8evXAvPfEjGhVMoTKXAb2ZGGRmR8g1eZshaHmgTg2imSiaoXU5eiF3HvBnDuawaCtt674ikZ3oZdekqswcVPGMwqqUKVsGY4QuFeQoGwRkMqEYTdV2UDMMsfrjrBYQYKUBFMwsQGMNBL1VoY78aotXzdeqJCBVKbQdD3ZZWvukhSe4xrz8tcF3PoxpysDLt89boMqZJtGEHTV9UBTBEac6sDyQP693qT3nKaErN8TCXrJBUmHPqKozAg9bwxTqMYkpmb9iVKLSoJxG7MjAj72SRbcqQfNCVTztSwN3cRxSrVtz4p87jNFbVtFzhPg7UqDwNFTaasySCqM"
    const val SPECTRUM_TOKEN_ADDRESS = "3gb1RZucekcRdda82TSNS4FZSREhGLoi1FxGDmMZdVeLtYYixPRviEdYireoM9RqC6Jf4kx85Y1jmUg5XzGgqdjpkhHm7kJZdgUR3VBwuLZuyHVqdSNv3eanqpknYsXtUwvUA16HFwNa3HgVRAnGC8zj8U7kksrfjycAM1yb19BB4TYR2BKWN7mpvoeoTuAKcAFH26cM46CEYsDRDn832wVNTLAmzz4Q6FqE29H9euwYzKiebgxQbWUxtupvfSbKaHpQcZAo5Dhyc6PFPyGVFZVRGZZ4Kftgi1NMRnGwKG7NTtXsFMsJP6A7yvLy8UZaMPe69BUAkpbSJdcWem3WpPUE7UpXv4itDkS5KVVaFtVyfx8PQxzi2eotP2uXtfairHuKinbpSFTSFKW3GxmXaw7vQs1JuVd8NhNShX6hxSqCP6sxojrqBxA48T2KcxNrmE3uFk7Pt4vPPdMAS4PW6UU82UD9rfhe3SMytK6DkjCocuRwuNqFoy4k25TXbGauTNgKuPKY3CxgkTpw9WfWsmtei178tLefhUEGJueueXSZo7negPYtmcYpoMhCuv4G1JZc283Q7f3mNXS"

    val USE_CONFIG = mapOf(
        "pool_address" to "3W5ZTNTWAwgjcNhctkBccWeUVruJJVLATdYp1makMwoP78WiW2MDjMd2HKxZ2eUwtaSrhtRujuvi27k49msqFVAi7T2BsVHvMCHQ879nf5oJvuXjhEshf76EZgrijL3v3KcEA8CYi511YFtwN1b9u7ZUXeQSSUhqcMvyXMwaCZrpZsgCfbiLxk2DQMrngBMUh96vh7cBfPxZWhsZ9DGUtkGhiquqH3DcgFhpP33rRMjanCRXPAx9SbbphH3RBA2Z9K9j9TvWV6PnUafVGSpixUS8eawxUCiAuUAZHttXK9DjWqzeTDxDH9Tz1gSyjy7aKokwZyoAGTEafuiNQQrJ1UVfuVJCHPUD5v9eomJLmLVqdVDEUm7gj6Qj9a2cEKDfzedex977RkqXvuaeUdaumcikVCr9spzgmv7rhFCovdzAJscwTio98iRGS9rqcnUoTZFN6YmNJPXKe3krdQ7c9yvv74Ad7SBQmvNyuMkchFRnbPRozogKzV3xmTMxpLzagjQ1AdcP",
        "lp_swap_address" to "8W5UV9yEpKMQLuKzk7oDmaFEBGqeC1RGauuADViEfYJcs8x55ySXMKUUnSni3itEbscEo4qT8X2GuWY9zNdbYWWCqZmJjsFdynhWPc3FBtE45nrPgf4gqVzqN7RX9LpWJBTj97b4tkMxqMEL8QFDmLb8UzWKpp79MD94AziQvArc33KCQ9nYz3MafjrV3YACCxKcNbwgsKH1AuNUWoRLbFYVJvqzCJRiDHPboNcVSWTFotKkrm3yHZafyifT9BTD6Rs62V6UbiWHi2U4njP84wVyLFE5PvJemVJKy3Bc2MHwXBaoKVuLqZXJMu62nbjANBzHoZZ1cVmA4y",
        "lp_nft" to "ef461517a55b8bfcd30356f112928f3333b5b50faf472e8374081307a09110cf"
    )

    val DEXYGOLD_CONFIG = mapOf(
        "pool_address" to "3W5ZTNTWAwgjcNhctkBccWeUVruJJVLATdYp29mnGMCFZADaExRGC6PPrusg4wV6srzDrgkRHhzQWBsugmYxXRE54rsc41SRf87KKvE6NdPHmtYM3HWsE746kotBqQ1Nk1Mun3AHQUDEP3seLSa1DzWwuNx7HmBBn9ZxnbVCZy3UdX4PHmkbj9NtJkZH2Upz9o7S2txbaoSnSAA6zwUXoypxkRtAXvx9neoUhjng7EvyDtFJcyKbXFB8vDZNPvHd6yjL12JUZjxDVWAFgUhBecPjUM5LRYmsyHArunqSsEC9WRRuK3TGo9jJCbpEh527UyNkDvYnwhbJ9kwmSXEx69zNPez8tNn5hXZrqFa5BqrDqALYqkShBwmw1BmeZPoqHRWNANn72ZAMibrbz8if7gWNEJmYuA36bESriXiwUBVxkNVD79zSiyjkv8QTemdaTR6NvWAQEAdbhNn4eqvzEgAMnzbiWv6AMNAE36noWggRchCwnnvmnna7yRvjW5j5861w6dMU",
        "lp_swap_address" to "8W5UV9yEpKMQLuKzk7oDmaFEBGqeC1RGauuADViEfYJcs8x55ySXMKUUnSni3itEbscEo4qT8X2GuWY9zNdbYWWCqZmJjsFdynhWPc3FBtE45nrPgf4gqVzqN7RX9LpWJBTj97b4tkMxqMEL8QFDmLb8UzWKpp79MD94AziQvArc33KCQ9nYz3MafjrV3YACCxKcNbwgsKH1AuNUWoRLbFYVJvqzCJRiDHPboNcVSWTFotKkrm3yHZafyifT9BTD6Rs62V6UbiWHi2U4njP84wVyLFE5PvJemVJKy3Bc2MHwXBaoKVuLqZXJMu62nbjANBzHoZZ1cVmA4y",
        "lp_nft" to "ff7b7eff3c818f9dc573ca03a723a7f6ed1615bf27980ebd4a6c91986b26f801"
    )

    val DEFAULT_FAVORITES = listOf("ERG", "SigUSD", "USE", "DEXYGOLD", "rsADA", "kushti", "RSN","SigRSV")
}
