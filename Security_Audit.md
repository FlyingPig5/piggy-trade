# TrufflΣ Security Audit Report

| | |
|---|---|
| **Application** | TrufflΣ — Native Android Ergo Wallet & DEX Client |
| **Blockchain** | Ergo (eUTXO Model) |
| **AI Agent** | Opus 4.6|
| **Date** | 2026-03-18 |
| **Auditor Role** | Senior Web3 Security Auditor (Fresh-Eyes Review) |
| **Methodology** | Defense-in-Depth, static source analysis of 30+ files |

---

## Executive Summary

| # | Finding | Severity | Category |
|---|---------|:--------:|----------|
| 1 | `cleartextTrafficPermitted="true"` but HTTPS enforced at app level | 🟢 Low | M3 — Network |
| 2 | `allowBackup="true"` with empty backup rules | 🟢 Low | M2 — Storage |
| 3 | `isMinifyEnabled = false` — no obfuscation on release builds | 🟢 Info | M9 — Reverse Engineering |
| 4 | Two alpha-stage security dependencies | 🟢 Low | Dependencies |
| 5 | `HttpLoggingInterceptor.Level.BASIC` in all builds | 🟢 Low | M4 — Network Logging |
| 6 | Several `Log.e/w` calls execute unconditionally in release | 🟢 Low | M2 — Side Channels |
| 7 | `EncryptedSharedPreferences` fallback to plaintext SharedPrefs | 🟢 Low | M2 — Storage |
| 8 | Mnemonic clipboard paste — no auto-clear | 🟢 Low | M2 — Side Channels |
| 9 | Developer fee is present and disclosed on review screen | 🟢 Info | Transparency |
| 10 | Slippage shown but no user-adjustable max-slippage guard | 🟢 Low | Blockchain Logic |
| 11 | `DeviceEncryption` key has `userAuthenticationRequired = false` | 🟢 Info | M2 — Storage |
| 12 | AMM `BigDecimal.toLong()` truncation on large volumes | 🟢 Info | Arithmetic |

> **Critical / High severity findings: 0**
> **Overall Rating: ✅ GOOD** — The application follows modern Android security best practices. No exploitable vulnerabilities were identified that could lead to remote fund theft.

---

## 1. Blockchain Logic

### 1.1 Access Control ✅

- **Mnemonic-based signing** is gated by password decryption (SCrypt+Fernet) or biometric authentication through Android Keystore. The mnemonic is never persisted in plaintext.
- **ErgoPay mode** delegates signing entirely to the official Ergo Mobile Wallet via EIP-20, keeping key material completely out of TrufflΣ.
- The `signTransaction()` call in `ErgoSigner` requires a valid decrypted mnemonic — there is no code path that bypasses this.

### 1.2 Arithmetic ✅

- The AMM module uses `BigDecimal` with `PRECISION = 100` decimal places and `RoundingMode.HALF_UP`, which is solid for constant-product math.
- `TxBuilder.buildSwapTx()` uses `BigInteger` for all nanoERG arithmetic — proper overflow prevention.
- **F12 (Info):** `Amm.sellToken()` calls `.toLong()` after BigDecimal computation. Overflow would require pool reserves >9.2 billion ERG — far beyond Ergo's ~97.7M ERG total supply. **Realistically impossible.**

### 1.3 Front-Running / MEV ✅

- Ergo's eUTXO model makes EVM-style sandwich attacks structurally impossible — the pool box is consumed atomically.
- Price impact (slippage) is **calculated and displayed** before trade confirmation. The user sees "Price Impact: X%" on the quote screen.
- **F10 (Low / UX):** No user-adjustable max-slippage parameter. If a large-trade user submits and the pool state has changed significantly, the transaction will simply **fail and not be mined** — the eUTXO model guarantees no partial execution and no fund loss. The user's funds stay in their wallet. A slippage guard would improve UX (avoid a failed TX) but is not a security issue.

### 1.4 Logic Errors ✅

- The `TxBuilder` enforces that `userChangeErg >= 0` and that token balances don't go negative after the swap. If either condition fails, it throws `IllegalArgumentException` before any signing occurs.
- The `Trader.selectMinimumBoxes()` function properly aggregates across multi-address wallets and only selects sufficient boxes.
- On Ergo's eUTXO model, spending a box that has already been spent (double-spend) is rejected by nodes at the mempool level. There is no application-layer double-spend vector.

### 1.5 Reentrancy ✅

**Not applicable.** Ergo's eUTXO model is structurally immune to reentrancy. A box is consumed in full when spent; there is no callback mechanism.

---

## 2. Key Management & Cryptography

### 2.1 Entropy ✅

- Mnemonic-to-address derivation is handled by `ergo-lib-jni` (sigma-rust via JNI) — the same library used by the official Ergo reference wallet.
- `MnemonicEncryption` uses `java.security.SecureRandom` for both the 16-byte salt and 16-byte IV. This is a CSPRNG backed by `/dev/urandom` on Linux/Android.
- `DeviceEncryption` uses Android Keystore's key generation, which uses the hardware-backed TEE/SE CSPRNG.

### 2.2 Storage ✅ (with caveats)

**Password-protected wallets:**
- Encrypted via SCrypt (N=32768, r=8, p=1) → 32-byte key → Fernet (AES-128-CBC + HMAC-SHA-256).
- HMAC verification uses **constant-time comparison** (`MessageDigest.isEqual()`). ✅
- Stored in `EncryptedSharedPreferences` (AES-256-SIV key names + AES-256-GCM values), backed by Android Keystore master key.

**Biometric wallets:**
- Mnemonic encrypted via `DeviceEncryption` (AES-256-GCM using an Android Keystore key).
- **F11:** The Keystore key has `userAuthenticationRequired = false` — because the biometric gate is enforced at the UI layer via `BiometricPrompt`, not at the crypto layer. The key itself is device-bound (non-exportable from Keystore TEE/SE), so extraction would require a rooted device with TEE exploit. Severity: **Info**.

**F7: EncryptedSharedPreferences fallback:**
- If `EncryptedSharedPreferences` initialization fails (e.g., certain devices without hardware keystore), the code falls back to standard `SharedPreferences`. On such devices, wallet data (including encrypted mnemonics) would be stored in XML accessible to any process on a rooted device. However, the mnemonic itself remains SCrypt+Fernet encrypted — the attacker would still need the user's password. Severity: **Low**.

### 2.3 Signature Malleability ✅

- Transaction signing is handled by `sigma-rust` (ergo-lib-jni), which implements Schnorr signatures per Ergo's specification. Schnorr signatures in Ergo are non-malleable by protocol design.
- There is no application-layer signature manipulation — the native library produces the final signed transaction bytes.

### 2.4 Side-Channel Analysis 🟢

**F6: Unconditional `Log.e`/`Log.w` in release builds:**

Several log statements execute without `BuildConfig.DEBUG` guards:

| Location | What is logged |
|----------|----------------|
| `ErgoSigner.kt:220` | "buildReducedTxBytes FAILED" + exception message |
| `NodeClient.kt:219,229,259,309` | HTTP retry info and node URLs |
| `TradeMapper.kt:134` | Unresolved trade route names |
| `PreferenceManager.kt:32` | EncryptedSharedPreferences failure reason |
| `SaveWallet` | `Log.e` for encryption failure |
| Multiple `StablecoinProtocol` files | `Log.e` for eligibility check failures |

**Attack vector:** Requires ADB-connected device or `logcat` on a rooted device. Release-mode `Log.e/w` entries might reveal node URLs and error patterns, but no mnemonics, keys, or wallet balances are logged. Sensitivity is low.

**F5: `HttpLoggingInterceptor.Level.BASIC` in all builds:**
`NodeClient.kt:125` applies BASIC-level HTTP logging unconditionally. BASIC logs the request method, URL, response code, and response body size. It does **not** log headers or bodies, so no sensitive data (transaction payloads, box data) is leaked. Severity: **Low** — consider restricting to debug builds.

**F8: Clipboard mnemonic paste:**
`AddWalletScreen.kt` allows pasting mnemonics from the clipboard but does not clear the clipboard afterward. On Android 12+ the clipboard is auto-cleared after ~60 seconds, and apps cannot read the clipboard in background. On Android 10-11, any foreground app could theoretically read it.
**Attack vector:** Requires malware running in the foreground on the same device, reading the clipboard within seconds of the paste. If the user also has accessibility-service-based malware, the risk increases. Severity: **Low**.

### 2.5 Hardcoded Secrets ✅

- No hardcoded private keys, mnemonics, or API secrets were found in the codebase.
- All node URLs in `NetworkConfig.kt` are public Ergo node endpoints — no authentication tokens.

---

## 3. Adversarial Red Team — Three Attack Scenarios

### 3.1 Attack Vector A: Transaction Interception via Network MITM

| Aspect | Detail |
|--------|--------|
| **Prerequisite** | Attacker controls a WiFi network the user connects to (coffee-shop MITM) |
| **Steps** | 1. ARP-spoof or rogue AP → intercept traffic. 2. Attempt to downgrade TLS or intercept cleartext. 3. Read/modify transaction data in transit. |
| **Mitigating controls** | All 18 hardcoded nodes use HTTPS. The app enforces HTTPS at the application layer (`initializeNodeClient()` checks for `http://` and blocks it unless `allowHttpNodes` is explicitly enabled by the user in settings). Android's default TLS config validates certificates against the system store. |
| **Result** | **Attack fails.** TLS prevents interception. Even if `cleartextTrafficPermitted="true"` is set in the network security config, the app's own URL validation rejects HTTP nodes by default. An attacker cannot read or modify transaction data. |
| **What attacker gains** | Nothing. TLS metadata (IP addresses, connection patterns) is visible but no transaction content. |

### 3.2 Attack Vector B: Redirecting Trade Value via Malicious Pool Data

| Aspect | Detail |
|--------|--------|
| **Prerequisite** | Attacker compromises one of the public Ergo nodes used by TrufflΣ |
| **Steps** | 1. Serve a fake pool box with manipulated token reserves. 2. The app quotes a trade based on spoofed reserves. 3. User approves unfavorable trade. 4. The actual on-chain pool box has different reserves → transaction either fails validation or executes at the real rate. |
| **Mitigating controls** | On Ergo, the transaction references the pool box **by box ID**. If the node returns a fake box, the box ID won't match the real on-chain box, and the transaction will be rejected by miners. The sigma-rust native library validates box IDs cryptographically. |
| **Result** | **Attack fails at the blockchain level.** The transaction would reference a non-existent box and be rejected. The only impact is a misleading quote (UX annoyance), not fund loss. |
| **What attacker gains** | Nothing. User might see an incorrect quote but cannot lose funds because the TX won't be mined. |

### 3.3 Attack Vector C: Seed Compromise During Wallet Setup

| Aspect | Detail |
|--------|--------|
| **Prerequisite** | Malware with screen overlay / accessibility service permission on the user's device |
| **Steps** | 1. User opens TrufflΣ and navigates to "Add Wallet". 2. User types or pastes their mnemonic. 3. Malware with accessibility permissions reads the text field content. 4. Alternatively, malware reads the clipboard if user pastes. |
| **Mitigating controls** | The mnemonic text field is a standard Compose `OutlinedTextField` — Android does not allow other apps to read its content without accessibility permission or a screen-overlay exploit. Clipboard reading requires foreground status (Android 10+) or accessibility permission. The mnemonic is encrypted immediately upon save. |
| **Result** | **Attack succeeds only with elevated permissions.** If the user's device has malware with accessibility-service access, the mnemonic can be captured during input. This is a platform-level threat that affects all wallet apps equally. |
| **What attacker gains** | Full control of the wallet if mnemonic is captured. Mitigation: use ErgoPay mode (mnemonic never enters TrufflΣ). |

---

## 4. OWASP Mobile Top 10

| # | Category | Rating | Analysis |
|---|----------|:------:|----------|
| **M1** | Improper Platform Usage | 🟢 | Correct use of Android Keystore, BiometricPrompt, EncryptedSharedPreferences. No improper intent exports (only one exported activity: `MainActivity` with LAUNCHER intent). FileProvider is not exported. |
| **M2** | Insecure Data Storage | 🟢 | **F2: `allowBackup="true"` with empty rules.** Android Auto Backup may upload app data to Google Drive. **However:** EncryptedSharedPreferences are encrypted with an Android Keystore master key that is **device-bound and non-exportable** — backup data restored to a different device is undecryptable. The SCrypt-encrypted mnemonic still requires the user's password. **Realistic risk: Very Low.** |
| **M3** | Insecure Communication | 🟢 | **F1: `cleartextTrafficPermitted="true"`** in `network_security_config.xml` allows HTTP at the platform level. **However,** the app blocks HTTP nodes by default in code — only enabled if the user explicitly toggles "Allow HTTP Nodes". All 18 built-in nodes use HTTPS. Risk: **Very Low** — only if user manually enables HTTP. |
| **M4** | Insecure Authentication | 🟢 | Biometric auth uses `BiometricPrompt` with `BIOMETRIC_STRONG`. Password auth requires minimum 8 characters with strength feedback. No bypass routes found. |
| **M5** | Insufficient Cryptography | 🟢 | SCrypt (N=32768, r=8, p=1) is strong KDF. AES-CBC+HMAC (Fernet) and AES-GCM (Keystore) are industry-standard. CSPRNG for all random values. Constant-time HMAC comparison. |
| **M6** | Insecure Authorization | 🟢 | Signing requires either password decryption or biometric verification. ErgoPay delegates to external wallet. No code path skips authorization for value-transferring operations. |
| **M7** | Client Code Quality | 🟢 | Kotlin null-safety, BigInteger/BigDecimal for financial math, proper error handling with user-facing messages. `BuildConfig.DEBUG` guards on most debug logging. |
| **M8** | Code Tampering | 🟢 | **Open-source by design.** Not applicable. |
| **M9** | Reverse Engineering | 🟢 | **Open-source by design.** ProGuard/R8 is disabled (`isMinifyEnabled = false`), which is intentional for an open-source project. Not a vulnerability. |
| **M10** | Extraneous Functionality | 🟢 | Debug-only features (JSON viewer, simulation mode, legacy toggle) are gated behind `isDebugMode`/ `BuildConfig.DEBUG`. No hidden admin endpoints or test backdoors. |

---

## 5. Dependency & Supply Chain

### 5.1 Version Catalog Review

| Library | Version | Status | Notes |
|---------|---------|:------:|-------|
| `bouncycastle` (bcprov-jdk15to18) | 1.78.1 | ✅ Current | Latest stable. Well-known crypto library. |
| `retrofit2` | 2.11.0 | ✅ Current | Latest stable. |
| `okhttp3` | 4.12.0 | ✅ Current | Latest stable 4.x branch. |
| `coil` | 2.6.0 | ✅ Current | Image loading. No security implications. |
| `androidx.biometric` | **1.2.0-alpha05** | 🟢 Alpha | F4a: Pre-release, but the used API (`BiometricPrompt.authenticate`) has been stable since 1.1.0. Practical risk: **Low**. |
| `androidx.security:security-crypto` | **1.1.0-alpha06** | 🟢 Alpha | F4b: Pre-release. The underlying `Tink` library is production-grade. The fallback to standard SharedPrefs mitigates any breakage risk. Practical risk: **Low**. |
| `ergo-lib-jni` | 0.1.0 | 🟢 Local source | Compiled from `sigma-rust/` in-repo. Being 0.x affects API stability, not security. Maintained by the Ergo community. |
| `compose-bom` | 2024.06.00 | ✅ Current | Compose Bill of Materials. |
| `kotlin` | 1.9.24 | ✅ Acceptable | One minor behind latest (1.9.25 exists). No security CVEs. |
| `AGP` | 8.13.2 | ✅ Current | Android Gradle Plugin. |

### 5.2 Typosquatting Check ✅

All package names match their canonical Maven coordinates:

- `org.bouncycastle:bcprov-jdk15to18` ✅
- `com.squareup.retrofit2:retrofit` ✅
- `com.squareup.okhttp3:okhttp` ✅
- `io.coil-kt:coil-compose` ✅
- `androidx.*` — all from Google's Maven repository ✅

### 5.3 Post-Install Scripts / Obfuscated Code ✅

- No custom Gradle plugins or build-phase code injection.
- The `sigma-rust` integration is compiled as a **local JNI source library**, not a remote artifact. This is actually **more secure** than a remote dependency — the signing code is auditable in the repository.
- No obfuscated or minified source code in the project.

### 5.4 Critical Dependency Assessment

| Component | Source | Risk |
|-----------|--------|:----:|
| Signing (sigma-rust/ergo-lib-jni) | **Local source** | 🟢 Auditable, no remote artifact trust required |
| KDF (BouncyCastle SCrypt) | Remote Maven (bcprov) | 🟢 Long-standing, audited library |
| Encrypted Storage (security-crypto) | Remote Maven (Google) | 🟢 Alpha, but Google-maintained; fallback exists |
| Biometric Auth (biometric) | Remote Maven (Google) | 🟢 Alpha, but Google-maintained; stable API surface |

---

## 6. Developer Fee

A developer fee exists in the application. It is:

- **Disclosed to the user** on the Review Transaction screen — labeled as "App Fee: X.XXXXX ERG" in the transaction breakdown.
- **Visible in the transaction details** — the `TransactionAddressBreakdown` component labels the fee output as "APP FEE" in orange.
- The fee is computed proportionally and has a minimum floor. These details are discoverable in the open source.

---

## 7. Consolidated Recommendations

### 🟢 Low Priority

| # | Recommendation | Justification |
|---|----------------|---------------|
| 1 | **Promote alpha dependencies to stable** when available — `biometric` and `security-crypto`. | Alpha APIs can break with OS updates. The `security-crypto` fallback mitigates breakage, but stable releases would eliminate the fallback path entirely. |
| 2 | **Add max-slippage guard (UX improvement).** Let users set a tolerance (e.g., 1-5%). On eUTXO, a pool-state change means the TX simply fails — no fund loss — but avoiding a failed TX improves UX. | Not a security issue; purely UX. |
| 3 | **Clear clipboard after mnemonic paste.** Call `clipboardManager.setText(AnnotatedString(""))` after paste. | Reduces the window for clipboard-sniffing on Android <12. |

### ℹ️ Informational / Hardening

| # | Recommendation | Justification |
|---|----------------|---------------|
| 4 | **Gate OkHttp logging to debug builds.** Wrap `HttpLoggingInterceptor` with `if (BuildConfig.DEBUG)`. | Eliminates any HTTP logging in release. |
| 5 | **Wrap remaining `Log.e/w` calls with `BuildConfig.DEBUG`.** | Zero release logging is the gold standard for wallet apps; unguarded calls don't leak secrets but add noise. |
| 6 | **Restrict `allowBackup`** — set `android:allowBackup="false"` or add explicit exclusions in `backup_rules.xml`. | Belt-and-suspenders: Keystore keys are device-bound so backups are already useless on a new device, but explicit exclusion removes even the theoretical concern. |
| 7 | **Set `cleartextTrafficPermitted="false"`** in `network_security_config.xml`. | The app already blocks HTTP nodes in code; this adds an OS-level enforcement layer. |

---

## 8. Overall Assessment

**TrufflΣ demonstrates a well-engineered security posture for an open-source mobile wallet.** The cryptographic layer is properly implemented with SCrypt KDF, Fernet (AES-CBC+HMAC) for password-protected wallets, and Android Keystore AES-GCM for biometric wallets. All sensitive logging is gated behind `BuildConfig.DEBUG` with only minor exceptions. The signing engine (`sigma-rust`) is integrated as local source rather than a remote artifact, which is a supply-chain security advantage.

The eUTXO model of Ergo provides inherent structural protections against reentrancy and most MEV-style attacks. The AMM math uses `BigDecimal`/`BigInteger` throughout, preventing overflow/underflow in financial calculations.

No critical or high severity vulnerabilities were identified. The findings are limited to configuration hardening opportunities (network security config, backup rules) and UX-level improvements (slippage guards, clipboard clearing). The developer fee is transparently disclosed to users on the review screen.

**This application is suitable for managing real funds**, with the caveat that mnemonic-mode users should use strong passwords (≥12 characters) and that the ErgoPay mode provides the highest security by keeping key material entirely outside TrufflΣ.
