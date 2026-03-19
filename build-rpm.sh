#!/bin/bash

# =========================================================
# Papel IT - RPM Builder Script
# =========================================================
# This script builds the .rpm installer for Papel IT.
# Uses Temurin JDK 17 to bypass Fedora's modified security
# file which blocks jpackage when using system JDK 25.
# =========================================================

set -e

TEMURIN_JDK_VERSION="17.0.14+7"
TEMURIN_DIR="/tmp/jdk-${TEMURIN_JDK_VERSION}"
TEMURIN_URL="https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.14%2B7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.14_7.tar.gz"

echo "==================================================="
echo " Papel IT - RPM Build Script"
echo "==================================================="

# ---- Step 1: Ensure Temurin JDK 17 is available ----
if [ ! -f "${TEMURIN_DIR}/bin/jpackage" ]; then
    echo ""
    echo "[Step 1] Downloading Eclipse Temurin JDK 17 (clean JDK for packaging)..."
    curl -L "$TEMURIN_URL" -o /tmp/temurin17.tar.gz
    tar -xzf /tmp/temurin17.tar.gz -C /tmp/
    echo "         Done."
else
    echo "[Step 1] Temurin JDK 17 already present. Skipping download."
fi

# ---- Step 2: Build JAR with dependencies ----
echo ""
echo "[Step 2] Building project and packaging dependencies..."
mvn clean package -DskipTests -q
echo "         Done."

# ---- Step 3: Run jpackage with clean JDK ----
echo ""
echo "[Step 3] Creating .rpm installer using Temurin JDK 17..."
mkdir -p target/jpackage

"${TEMURIN_DIR}/bin/jpackage" \
  --type rpm \
  --dest target/jpackage \
  --name "Papel IT" \
  --app-version 1.0.0 \
  --vendor "PapelIT" \
  --main-jar papelit-1.0.0.jar \
  --main-class com.example.papelit.StickyNotesLauncher \
  --input target/libs \
  --icon src/main/resources/icon.png \
  --linux-package-name papelit \
  --linux-shortcut \
  --description "Papel IT - A beautiful sticky notes application"

echo ""
echo "==================================================="
echo " SUCCESS! RPM installer created:"
ls target/jpackage/*.rpm
echo "==================================================="
echo ""
echo " To install it, run:"
echo "   sudo dnf install target/jpackage/papelit-1.0.0-1.x86_64.rpm"
echo ""
