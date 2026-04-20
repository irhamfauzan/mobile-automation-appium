package com.mobile.automation.base;

import com.mobile.automation.core.DriverManager;
import com.mobile.automation.utils.ExtentReportManager;
import com.mobile.automation.utils.LogUtil;
import com.mobile.automation.utils.ScreenshotUtil;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import io.github.cdimascio.dotenv.Dotenv;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Properties;

/**
 * Base class for all tests. Handles driver lifecycle, ExtentReports, and screenshots.
 */
public class BaseTest {

    protected Properties config;
    protected static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    @BeforeSuite(alwaysRun = true)
    public void initSuite() {
        ExtentReportManager.initReports();
        LogUtil.info("=== Test Suite Started ===");
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp(Method method) {
        loadConfig();
        LogUtil.info("Starting test: " + method.getName());

        ExtentTest test = ExtentReportManager.createTest(
                method.getName(),
                getTestDescription(method)
        );
        extentTest.set(test);

        try {
            DriverManager.initDriver(config);
            LogUtil.info("Driver ready for: " + method.getName());
        } catch (MalformedURLException e) {
            LogUtil.error("Failed to initialize driver: " + e.getMessage());
            throw new RuntimeException("Driver initialization failed", e);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        ExtentTest test = extentTest.get();
        String testName = result.getMethod().getMethodName();

        if (result.getStatus() == ITestResult.FAILURE) {
            String screenshotPath = ScreenshotUtil.capture(DriverManager.getDriver(), testName + "_FAIL");
            LogUtil.error("TEST FAILED: " + testName + " | " + result.getThrowable().getMessage());

            if (test != null) {
                test.fail(result.getThrowable());
                if (screenshotPath != null) {
                    test.addScreenCaptureFromPath(screenshotPath, "Failure Screenshot");
                }
            }
        } else if (result.getStatus() == ITestResult.SKIP) {
            LogUtil.warn("TEST SKIPPED: " + testName);
            if (test != null) test.skip(result.getThrowable());
        } else {
            LogUtil.info("TEST PASSED: " + testName);
            if (test != null) test.pass("Test passed successfully");
        }

        DriverManager.quitDriver();
        extentTest.remove();
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        ExtentReportManager.flushReports();
        LogUtil.info("=== Test Suite Completed ===");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void loadConfig() {
        config = new Properties();
        Dotenv dotenv = loadDotenv();

        // Load config.properties
        try (FileInputStream fis = new FileInputStream("src/main/resources/config.properties")) {
            config.load(fis);
        } catch (IOException e) {
            LogUtil.warn("config.properties not found, using defaults");
        }

        // Override with environment variables (higher precedence)
        overrideFromEnv(dotenv, "PLATFORM", "platform");
        overrideFromEnv(dotenv, "APPIUM_URL", "appium.url");
        overrideFromEnv(dotenv, "ANDROID_DEVICE_NAME", "android.device.name");
        overrideFromEnv(dotenv, "ANDROID_PLATFORM_VERSION", "android.platform.version");
        overrideFromEnv(dotenv, "ANDROID_APP_PATH", "android.app.path");
        overrideFromEnv(dotenv, "ANDROID_APP_PACKAGE", "android.app.package");
        overrideFromEnv(dotenv, "ANDROID_APP_ACTIVITY", "android.app.activity");
        overrideFromEnv(dotenv, "IOS_DEVICE_NAME", "ios.device.name");
        overrideFromEnv(dotenv, "IOS_PLATFORM_VERSION", "ios.platform.version");
        overrideFromEnv(dotenv, "IOS_APP_PATH", "ios.app.path");
        overrideFromEnv(dotenv, "IOS_BUNDLE_ID", "ios.bundle.id");
    }

    private Dotenv loadDotenv() {
        try {
            return Dotenv.configure().ignoreIfMissing().load();
        } catch (Exception e) {
            return Dotenv.configure().ignoreIfMissing().load();
        }
    }

    private void overrideFromEnv(Dotenv dotenv, String envKey, String configKey) {
        String value = dotenv.get(envKey);
        if (value == null) value = System.getenv(envKey);
        if (value != null && !value.isEmpty()) {
            config.setProperty(configKey, value);
        }
    }

    private String getTestDescription(Method method) {
        org.testng.annotations.Test annotation = method.getAnnotation(org.testng.annotations.Test.class);
        return (annotation != null && !annotation.description().isEmpty())
                ? annotation.description()
                : method.getName();
    }

    // ─── Extent test accessor for subclasses ─────────────────────────────────

    protected ExtentTest getExtentTest() {
        return extentTest.get();
    }

    protected void logStep(String stepDescription) {
        LogUtil.info("  STEP: " + stepDescription);
        ExtentTest test = extentTest.get();
        if (test != null) test.log(Status.INFO, stepDescription);
    }

    protected void logInfo(String message) {
        LogUtil.info(message);
        ExtentTest test = extentTest.get();
        if (test != null) test.info(message);
    }
}
