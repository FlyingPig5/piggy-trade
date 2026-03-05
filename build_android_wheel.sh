#!/bin/bash
# build_android_wheel.sh
# Cross-compiles ergo-lib-python for Android aarch64 using Docker + maturin.
# Run from the root of the piggytrade project directory.
set -e

# Resolve the project root dynamically (works regardless of username or path)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_DIR="$SCRIPT_DIR/src/piggytrade/resources/bin"

TARGET_ARCH="aarch64"

# Clean up any previous build
rm -rf /tmp/sigma-rust

echo "[1] Pulling sigma-rust source code..."
git clone https://github.com/ergoplatform/sigma-rust.git /tmp/sigma-rust
cd /tmp/sigma-rust
git checkout develop

echo "[2] Launching maturin container to cross-compile for android/${TARGET_ARCH}..."
# Mount the root of sigma-rust so workspace inheritance works
sudo docker run --rm -v "$(pwd)":/io ghcr.io/pyo3/maturin build \
    --release \
    -m /io/bindings/ergo-lib-python/Cargo.toml \
    --target ${TARGET_ARCH}-linux-android \
    --zig \
    --out /io/bindings/ergo-lib-python/wheels \
    --auditwheel skip

echo "[3] Copying compiled wheel..."
mkdir -p "$OUTPUT_DIR"
cp bindings/ergo-lib-python/wheels/*.whl "$OUTPUT_DIR/"

echo "✅ SUCCESS! Android Wheel compiled and saved to dev_tools/wheels/"
ls -la "$OUTPUT_DIR/"

