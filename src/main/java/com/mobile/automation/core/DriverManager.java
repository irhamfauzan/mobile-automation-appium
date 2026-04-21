package com.mobile.automation.core;

import com.mobile.automation.utils.LogUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;

/**
 * Thread-safe driver manager using ThreadLocal for parallel execution.
 * Each thread gets its own AppiumDriver instance, enabling true parallel test runs.
 */
public class DriverManager {

    private static final ThreadLocal<AppiumDriver> driverThreadLocal = new ThreadLocal<>();
    private static final String DEFAULT_APPIUM_URL = "http://127.0.0.1:4723";

    private DriverManager() {}

    public static AppiumDriver getDriver() {
        return driverThreadLocal.get();
    }

    public static void setDriver(AppiumDriver driver) {
        driverThreadLocal.set(driver);
    }

    public static void initDriver(Properties config) throws MalformedURLException {
        String platform = config.getProperty("platform", "android").toLowerCase();
        String appiumUrl = config.getProperty("appium.url", DEFAULT_APPIUM_URL);

        LogUtil.info("Initializing " + platform + " driver | Appium: " + appiumUrl);

        AppiumDriver driver = platform.equals("ios")
                ? createIOSDriver(config, appiumUrl)
                : createAndroidDriver(config, appiumUrl);

        int implicitWait = Integer.parseInt(config.getProperty("implicit.wait", "10"));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));

        setDriver(driver);
        LogUtil.info("Driver initialized successfully for thread: " + Thread.currentThread().getId());
    }

    private static AndroidDriver createAndroidDriver(Properties config, String appiumUrl)
            throws MalformedURLException {
        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("platformName", "Android");
        caps.setCapability("appium:automationName",
                config.getProperty("android.automation.name", "UiAutomator2"));
        caps.setCapability("appium:deviceName",
                config.getProperty("android.device.name", "Android Emulator"));
        caps.setCapability("appium:platformVersion",
                config.getProperty("android.platform.version", "12.0"));

        String appPath = config.getProperty("android.app.path");
        String appPackage = config.getProperty("android.app.package");
        String appActivity = config.getProperty("android.app.activity");

        if (appPath != null && !appPath.isEmpty()) {
            caps.setCapability("appium:app", appPath);
        } else if (appPackage != null && !appPackage.isEmpty()) {
            caps.setCapability("appium:appPackage", appPackage);
            caps.setCapability("appium:appActivity", appActivity);
        }

        caps.setCapability("appium:noReset", Boolean.parseBoolean(config.getProperty("no.reset", "false")));
        caps.setCapability("appium:fullReset", Boolean.parseBoolean(config.getProperty("full.reset", "false")));
        caps.setCapability("appium:newCommandTimeout", 300);
        caps.setCapability("appium:autoGrantPermissions", true);

        return new AndroidDriver(new URL(appiumUrl), caps);
    }

    private static IOSDriver createIOSDriver(Properties config, String appiumUrl)
            throws MalformedURLException {
        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("platformName", "iOS");
        caps.setCapability("appium:automationName",
                config.getProperty("ios.automation.name", "XCUITest"));
        caps.setCapability("appium:deviceName",
                config.getProperty("ios.device.name", "iPhone Simulator"));
        caps.setCapability("appium:platformVersion",
                config.getProperty("ios.platform.version", "16.0"));
        caps.setCapability("appium:udid",
                config.getProperty("ios.udid", ""));

        String appPath = config.getProperty("ios.app.path");
        String bundleId = config.getProperty("ios.bundle.id");

        if (appPath != null && !appPath.isEmpty()) {
            caps.setCapability("appium:app", appPath);
        } else if (bundleId != null && !bundleId.isEmpty()) {
            caps.setCapability("appium:bundleId", bundleId);
        }

        caps.setCapability("appium:noReset", Boolean.parseBoolean(config.getProperty("no.reset", "false")));
        caps.setCapability("appium:newCommandTimeout", 300);
        caps.setCapability("appium:wdaLaunchTimeout", 120000);

        return new IOSDriver(new URL(appiumUrl), caps);
    }

    public static void quitDriver() {
        AppiumDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                LogUtil.info("Driver quit successfully for thread: " + Thread.currentThread().getId());
            } catch (Exception e) {
                LogUtil.error("Error quitting driver: " + e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    public static boolean isDriverActive() {
        AppiumDriver driver = driverThreadLocal.get();
        if (driver == null) return false;
        try {
            // getCapabilities() is always available on AppiumDriver (inherited from RemoteWebDriver)
            // and throws if the session is dead — safe on both Android and iOS
            driver.getCapabilities();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
