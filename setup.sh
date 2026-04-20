#!/usr/bin/env bash
# ════════════════════════════════════════════════════════════════
#  Mobile Automation Framework — Setup Script
#  Installs all required tools and verifies the environment
# ════════════════════════════════════════════════════════════════

set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
RESET='\033[0m'

info()    { echo -e "${GREEN}[INFO]${RESET}  $*"; }
warn()    { echo -e "${YELLOW}[WARN]${RESET}  $*"; }
error()   { echo -e "${RED}[ERROR]${RESET} $*" >&2; }
section() { echo -e "\n${CYAN}══ $* ══${RESET}"; }

# ─── Java ────────────────────────────────────────────────────────────────────
section "Checking Java"
if command -v java &>/dev/null; then
    JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
    if [[ "$JAVA_VER" -ge 11 ]]; then
        info "Java $JAVA_VER found ✓"
    else
        error "Java 11+ required, found $JAVA_VER"
        exit 1
    fi
else
    error "Java not found. Install JDK 11+: https://adoptium.net"
    exit 1
fi

# ─── Maven ───────────────────────────────────────────────────────────────────
section "Checking Maven"
if command -v mvn &>/dev/null; then
    info "Maven $(mvn --version | head -1) ✓"
else
    warn "Maven not found. Installing via SDKMAN..."
    if command -v sdk &>/dev/null; then
        sdk install maven
    else
        error "Install Maven manually: https://maven.apache.org/install.html"
        exit 1
    fi
fi

# ─── Node.js & Appium ────────────────────────────────────────────────────────
section "Checking Node.js"
if command -v node &>/dev/null; then
    info "Node.js $(node --version) ✓"
else
    error "Node.js not found. Install from: https://nodejs.org"
    exit 1
fi

section "Installing / updating Appium"
if npm list -g appium &>/dev/null; then
    info "Appium already installed, updating..."
    npm update -g appium
else
    npm install -g appium
fi
info "Appium $(appium --version) ✓"

section "Installing Appium drivers"
if ! appium driver list --installed 2>/dev/null | grep -q "uiautomator2"; then
    appium driver install uiautomator2
    info "UiAutomator2 driver installed ✓"
else
    info "UiAutomator2 already installed ✓"
fi

if [[ "$(uname)" == "Darwin" ]]; then
    if ! appium driver list --installed 2>/dev/null | grep -q "xcuitest"; then
        appium driver install xcuitest
        info "XCUITest driver installed ✓"
    else
        info "XCUITest already installed ✓"
    fi
fi

# ─── Android SDK ─────────────────────────────────────────────────────────────
section "Checking Android SDK"
if [[ -n "${ANDROID_HOME:-}" ]] || [[ -n "${ANDROID_SDK_ROOT:-}" ]]; then
    info "ANDROID_HOME set ✓"
    if command -v adb &>/dev/null; then
        info "ADB $(adb --version | head -1) ✓"
    else
        warn "ADB not in PATH. Add \$ANDROID_HOME/platform-tools to PATH"
    fi
else
    warn "ANDROID_HOME not set. Required for Android testing."
    warn "Add to your shell profile:"
    warn "  export ANDROID_HOME=\$HOME/Library/Android/sdk  # macOS"
    warn "  export ANDROID_HOME=\$HOME/Android/Sdk           # Linux"
    warn "  export PATH=\$ANDROID_HOME/platform-tools:\$PATH"
fi

# ─── .env setup ──────────────────────────────────────────────────────────────
section "Setting up .env"
if [[ ! -f ".env" ]]; then
    cp .env.example .env
    info ".env created from .env.example — update it with your settings"
else
    info ".env already exists ✓"
fi

# ─── Test data ───────────────────────────────────────────────────────────────
section "Generating Excel test data"
EXCEL_FILE="src/test/resources/testdata/login_data.xlsx"
if [[ ! -f "$EXCEL_FILE" ]]; then
    if command -v python3 &>/dev/null; then
        if python3 -c "import openpyxl" 2>/dev/null; then
            python3 src/test/resources/testdata/generate_excel_data.py
            info "Excel test data generated ✓"
        else
            warn "openpyxl not installed. Running: pip3 install openpyxl"
            pip3 install openpyxl --quiet
            python3 src/test/resources/testdata/generate_excel_data.py
            info "Excel test data generated ✓"
        fi
    else
        warn "Python3 not found. Excel test data not generated."
        warn "Run manually: pip3 install openpyxl && python3 src/test/resources/testdata/generate_excel_data.py"
    fi
else
    info "Excel test data already exists ✓"
fi

# ─── Output directories ──────────────────────────────────────────────────────
section "Creating output directories"
mkdir -p test-output/{extent-reports,screenshots,logs}
info "Output directories ready ✓"

# ─── Maven dependencies ──────────────────────────────────────────────────────
section "Downloading Maven dependencies"
mvn dependency:resolve --quiet --no-transfer-progress
info "Dependencies resolved ✓"

# ─── Final summary ───────────────────────────────────────────────────────────
echo ""
echo -e "${GREEN}════════════════════════════════════════════${RESET}"
echo -e "${GREEN}  Setup complete! Next steps:${RESET}"
echo -e "${GREEN}════════════════════════════════════════════${RESET}"
echo ""
echo "  1. Update .env with your device/app settings"
echo "  2. Start Appium:  appium --address 127.0.0.1 --port 4723"
echo "  3. Connect device or start emulator"
echo "  4. Run smoke tests:"
echo "       mvn test -Psmoke"
echo "  5. Run all tests:"
echo "       mvn test"
echo ""
