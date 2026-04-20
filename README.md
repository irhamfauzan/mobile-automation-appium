# Mobile Automation Framework — Phase 1

A production-ready mobile test automation framework built on **Appium 8**, **TestNG 7**, and **ExtentReports 5**. Supports Android and iOS with parallel execution, data-driven testing, and rich HTML reporting.

---

## Project Structure

```
mobile-automation-appium/
├── src/
│   ├── main/java/com/mobile/automation/
│   │   ├── core/
│   │   │   ├── DriverManager.java       # Thread-safe parallel driver
│   │   │   └── BasePage.java            # Common page actions
│   │   ├── pages/
│   │   │   ├── LoginPage.java           # Fluent API login
│   │   │   ├── HomePage.java
│   │   │   ├── ProfilePage.java
│   │   │   └── ForgotPasswordPage.java
│   │   ├── utils/
│   │   │   ├── LogUtil.java             # Log4j2 wrapper
│   │   │   ├── ExtentReportManager.java # HTML reports
│   │   │   ├── ScreenshotUtil.java      # Failure capture
│   │   │   ├── ExcelReader.java         # Apache POI
│   │   │   └── JsonReader.java          # Jackson
│   │   └── listeners/
│   │       ├── RetryAnalyzer.java       # Auto-retry (2x)
│   │       ├── AnnotationTransformer.java
│   │       └── TestListener.java        # Suite lifecycle
│   ├── main/resources/
│   │   ├── config.properties
│   │   └── log4j2.xml
│   └── test/
│       ├── java/com/mobile/automation/
│       │   ├── base/BaseTest.java        # Setup + teardown
│       │   └── tests/LoginTest.java      # 5 test scenarios
│       └── resources/
│           ├── testng.xml                # 3 test groups
│           └── testdata/
│               ├── login_data.json
│               └── login_data.xlsx
├── .github/workflows/mobile-automation.yml
├── pom.xml
├── setup.sh
├── .env.example
└── .gitignore
```

---

## Quick Start

### Prerequisites

| Tool | Minimum Version |
|------|----------------|
| Java JDK | 11+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| Appium | 8.x |
| Android SDK | API 29+ |

### 1. Clone & setup

```bash
git clone https://github.com/irhamfauzan/mobile-automation-appium.git
cd mobile-automation-appium
chmod +x setup.sh && ./setup.sh
```

### 2. Configure your device

```bash
cp .env.example .env
# Edit .env — set device name, app path, and credentials
```

### 3. Start Appium

```bash
appium --address 127.0.0.1 --port 4723
```

### 4. Run tests

```bash
# Smoke tests (fast, critical paths)
mvn test -Psmoke

# Full regression suite
mvn test -Pregression

# Data-driven tests only
mvn test -Pdata-driven

# All tests
mvn test

# iOS (requires macOS + Xcode)
mvn test -Pios
```

---

## Framework Features

### Thread-safe Parallel Execution

`DriverManager` uses `ThreadLocal<AppiumDriver>` so each test thread owns its driver instance. Configure parallelism in `testng.xml`:

```xml
<suite parallel="tests" thread-count="3">
```

### Fluent Page Object API

```java
// Method chaining for readable test steps
HomePage home = new LoginPage()
    .enterUsername("user@example.com")
    .enterPassword("password")
    .tapLogin();
```

### Data-driven Tests

**JSON** — `login_data.json` loaded via `JsonReader`:
```java
@DataProvider(name = "loginDataJson")
public Object[][] loginDataJson() {
    return JsonReader.getTestData("src/test/resources/testdata/login_data.json", "loginTests");
}
```

**Excel** — `login_data.xlsx` loaded via `ExcelReader`:
```java
@DataProvider(name = "loginDataExcel")
public Object[][] loginDataExcel() {
    return ExcelReader.getTestDataAsMaps("src/test/resources/testdata/login_data.xlsx", "LoginData");
}
```

### Auto-retry Flaky Tests

`AnnotationTransformer` applies `RetryAnalyzer` to every `@Test` automatically — no per-test annotation needed. Default: **2 retries**.

To change the limit edit `RetryAnalyzer.java`:
```java
private static final int MAX_RETRY = 2;
```

### Rich HTML Reports

ExtentReports generates a dark-themed HTML report at:
```
test-output/extent-reports/AutomationReport_<timestamp>.html
```

Screenshots on failure are automatically embedded in the report.

### Colored Logging

Log4j2 with ANSI colors in console output and rolling file appenders:
```
test-output/logs/automation_<date>.log
test-output/logs/errors_<date>.log
```

---

## Configuration

### `config.properties` (default values)

```properties
platform=android
appium.url=http://127.0.0.1:4723
android.device.name=Android Emulator
android.platform.version=12.0
android.app.package=com.example.app
android.app.activity=com.example.app.MainActivity
implicit.wait=10
```

### Environment variables (override `config.properties`)

| Variable | Description |
|----------|-------------|
| `PLATFORM` | `android` or `ios` |
| `APPIUM_URL` | Appium server URL |
| `ANDROID_DEVICE_NAME` | Device/emulator name |
| `ANDROID_APP_PATH` | Path to APK |
| `ANDROID_APP_PACKAGE` | App package name |
| `ANDROID_APP_ACTIVITY` | Launcher activity |
| `IOS_DEVICE_NAME` | Simulator/device name |
| `IOS_BUNDLE_ID` | iOS bundle ID |
| `VALID_USERNAME` | Test account username |
| `VALID_PASSWORD` | Test account password |

---

## CI/CD — GitHub Actions

The workflow `.github/workflows/mobile-automation.yml` provides:

| Job | Trigger | Description |
|-----|---------|-------------|
| `build` | All pushes | Compiles and validates |
| `smoke-android` | All pushes | Smoke tests on Android emulator |
| `regression-android` | `main`/`develop` only | Full regression suite |
| `report-summary` | Always | Aggregates and uploads artifacts |

### Manual trigger

```
Actions → Mobile Automation Tests → Run workflow
  platform: android | ios
  test_group: smoke | regression | data-driven
```

---

## Adapting to Your App

1. **Locators** — Update `By` locators in each Page Object to match your app's accessibility IDs or XPaths
2. **Capabilities** — Set `android.app.package` + `android.app.activity` (or `android.app.path`) in `.env`
3. **Test data** — Edit `login_data.json` and `login_data.xlsx` with real credentials and scenarios
4. **Add pages** — Create a new class extending `BasePage`, implement `isPageLoaded()`
5. **Add tests** — Create a class extending `BaseTest`, annotate with `@Test(groups = {"smoke"})`

---

## Output Artifacts

```
test-output/
├── extent-reports/
│   └── AutomationReport_2026-04-20_10-30-00.html
├── screenshots/
│   └── testName_FAIL_20260420_103000.png
└── logs/
    ├── automation_2026-04-20.log
    └── errors_2026-04-20.log
```

---

## License

MIT License — see [LICENSE](LICENSE)
