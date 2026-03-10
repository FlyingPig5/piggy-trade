# Changelog

## 0.8.0
### Enhancements
- **PID-Centric Logic**: Successfully migrated all trading and whitelisting logic to use unique Pool IDs (PIDs) as the primary identifier, enabling robust support for multiple pools of the same asset.
- **Dynamic Pool Renaming**: Users can now rename duplicate pools to avoid confusion (e.g., RSN vs RSN-Alt), with custom names preserved throughout the trading interface.
- **Advanced Verification States**: Refined the token selector to clearly distinguish between **Official**, **User Added**, and **Unverified** pools using visual labels and colors.
- **Transparent Fee Structure**: Redesigned the Swap Order Details into an expandable panel, providing a granular breakdown of LP Fees, Service Fees, and a user-adjustable Miner Fee via a new slider.
- **Smart Quote Engine**: Introduced debounced price fetching and a short-term pool cache in the Trader to significantly reduce network load and improve UI responsiveness.
- **Enhanced History Parsing**: Transaction history now attempts to resolve sender/receiver addresses from ErgoTrees and displays per-transaction service fees for better accounting.
- **High-Precision Balances**: Increased ERG balance visibility to 5 decimal places across the wallet and swap screens for better tracking of small amounts.

### Bug Fixes
- **Logo Resolution**: Fixed an issue where renamed pools would fail to show their official token logo; the resolution logic now correctly maps custom pool keys back to their underlying assets.
- **Selector Consistency**: Corrected behavior where the swap panel would roll back custom-renamed tokens to their official tickers.
- **Address Resolution**: Improved the history engine to correctly identify "Self" transfers and properly associate inputs/outputs with the active wallet even when address strings are missing from on-chain data.


## 0.7.0
### Enhancements
- **On-Chain Authority**: Restructured token whitelisting to use `tokens.json` as a PID registry. The app now pulls all live parameters (fees, decimals, token IDs) directly from the blockchain, ensuring immunity to static configuration errors.
- **Aesthetic Refinement**: Increased swap screen typography for better readability. Redesigned the Settings UI with a modern, soft-white branding section and integrated social links.
- **Transparency**: Added a dedicated LP Fee percentage display in the Swap Order Details for better cost visibility.
- **Global Feedback**: Promoted the synchronization progress UI to a global state, ensuring visibility across all app screens during data updates.
- **Optimization**: Refined the token discovery engine to deduplicate assets across multiple boxes and accurately report only truly new trading pairs.

### Bug Fixes
- **Token Management**: Fixed a critical bug in the drag-and-drop functionality when whitelisting newly discovered tokens.
- **Discovery Accuracy**: Resolved an issue where official tokens were occasionally reported as "new" during synchronization.
- **Sync Logic**: Fixed a crash related to unbalanced data structures during deep-scan analyst mode.

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

## 0.5.0
* Initial release of PiggyTrade.

