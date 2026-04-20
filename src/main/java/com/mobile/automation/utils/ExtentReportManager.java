package com.mobile.automation.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manages a single ExtentReports instance shared across all threads.
 * ExtentTest instances are caller-managed (one per test method).
 */
public class ExtentReportManager {

    private static ExtentReports extent;
    private static final String REPORT_DIR = "test-output/extent-reports/";

    private ExtentReportManager() {}

    public static synchronized void initReports() {
        if (extent != null) return;

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String reportPath = REPORT_DIR + "AutomationReport_" + timestamp + ".html";

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("Mobile Automation Report");
        spark.config().setReportName("Appium Test Execution Report");
        spark.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");
        spark.config().setEncoding("UTF-8");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Framework", "Appium + TestNG");
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("Author", System.getProperty("user.name"));

        LogUtil.info("ExtentReports initialized: " + reportPath);
    }

    public static synchronized ExtentTest createTest(String testName, String description) {
        if (extent == null) initReports();
        return extent.createTest(testName, description);
    }

    public static synchronized void flushReports() {
        if (extent != null) {
            extent.flush();
            LogUtil.info("ExtentReports flushed");
        }
    }
}
