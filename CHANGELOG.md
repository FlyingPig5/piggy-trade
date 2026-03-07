# Changelog
## 0.5.0
* Initial release of PiggyTrade.

## 0.6.0
### Enhancements
- **AMM Accuracy**: Refined Automated Market Maker calculations to align perfectly with the original Python implementation.
- **Security**: Improved token synchronization priority—the built-in `tokens.json` now acts as a source of truth, overriding cached data to prevent spoofing of official tokens like DexyGold and USE.
- **Robustness**: Added comprehensive zero-division guards across all trading paths to prevent crashes on low-liquidity pools or small trade amounts.
- **Sync UI**: Added a spinning wheel and clearer batch progress indicators to the token synchronization screen.

### bug Fixes
- **Transaction Building**: Fixed "Not enough coins" error during DexyGold/USE swaps by correctly fetching and including the LP Swap box in transactions.
- **Fee Logic**: Corrected fee routing for special pools to ensure accurate price quotes and successful ledger balancing.
- **Performance**: Removed redundant network requests for special tokens already defined in system configuration.

