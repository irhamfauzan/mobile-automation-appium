package com.mobile.automation.utils;

import io.appium.java_client.AppiumDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Captures screenshots and saves them to test-output/screenshots/.
 * Returns the absolute path for embedding in ExtentReports.
 */
public class ScreenshotUtil {

    private static final String SCREENSHOT_DIR = "test-output/screenshots/";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtil() {}

    public static String capture(AppiumDriver driver, String screenshotName) {
        if (driver == null) {
            LogUtil.warn("Cannot capture screenshot: driver is null");
            return null;
        }

        try {
            String sanitized = screenshotName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
            String timestamp = LocalDateTime.now().format(FORMATTER);
            String fileName = sanitized + "_" + timestamp + ".png";

            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destDir = new File(SCREENSHOT_DIR);

            if (!destDir.exists()) destDir.mkdirs();

            File destFile = new File(destDir, fileName);
            FileUtils.copyFile(srcFile, destFile);

            String absolutePath = destFile.getAbsolutePath();
            LogUtil.debug("Screenshot saved: " + absolutePath);
            return absolutePath;

        } catch (IOException e) {
            LogUtil.error("Failed to save screenshot: " + e.getMessage());
            return null;
        } catch (Exception e) {
            LogUtil.error("Screenshot capture failed: " + e.getMessage());
            return null;
        }
    }

    public static String captureBase64(AppiumDriver driver) {
        if (driver == null) return null;
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            LogUtil.error("Base64 screenshot failed: " + e.getMessage());
            return null;
        }
    }
}
