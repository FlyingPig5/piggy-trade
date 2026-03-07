# Native Android Binaries (Rust)

This directory contains pre-compiled native wheels for Android.

## 📦 Current Binaries
- `ergo_lib_python-0.28.0-cp312-cp312-android_24_arm64_v8a.whl`
  - **Purpose**: Native Python bindings for `sigma-rust` (ergo-lib).
  - **Platform**: Android 64-bit (aarch64).
  - **Python**: 3.12 (Chaquopy).
  - **Migration Note**: Replaces the legacy 15MB `ergo.jar` (JVM dependency).

## 🛠️ Rebuilding from Source
If you need to update or audit these binaries, run:
```bash
./build_android_wheel.sh
```
This requires Docker and the Android NDK (v28+).
