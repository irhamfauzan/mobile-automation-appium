package com.mobile.automation.listeners;

import com.mobile.automation.utils.LogUtil;
import org.testng.*;

/**
 * Suite-level listener for lifecycle logging and summary reporting.
 */
public class TestListener implements ISuiteListener, ITestListener {

    // ─── Suite ───────────────────────────────────────────────────────────────

    @Override
    public void onStart(ISuite suite) {
        LogUtil.info("══════════════════════════════════════════");
        LogUtil.info("  SUITE STARTING: " + suite.getName());
        LogUtil.info("══════════════════════════════════════════");
    }

    @Override
    public void onFinish(ISuite suite) {
        int passed = 0, failed = 0, skipped = 0;
        for (ISuiteResult result : suite.getResults().values()) {
            ITestContext ctx = result.getTestContext();
            passed  += ctx.getPassedTests().size();
            failed  += ctx.getFailedTests().size();
            skipped += ctx.getSkippedTests().size();
        }
        LogUtil.info("══════════════════════════════════════════");
        LogUtil.info("  SUITE COMPLETE: " + suite.getName());
        LogUtil.info("  Passed:  " + passed);
        LogUtil.info("  Failed:  " + failed);
        LogUtil.info("  Skipped: " + skipped);
        LogUtil.info("══════════════════════════════════════════");
    }

    // ─── Test method ─────────────────────────────────────────────────────────

    @Override
    public void onTestStart(ITestResult result) {
        LogUtil.info("▶ START: " + getFullName(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LogUtil.info("✔ PASS:  " + getFullName(result)
                + " (" + getDuration(result) + "ms)");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LogUtil.error("✘ FAIL:  " + getFullName(result)
                + " (" + getDuration(result) + "ms)");
        LogUtil.error("  Cause: " + result.getThrowable().getMessage());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LogUtil.warn("⟳ SKIP:  " + getFullName(result));
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        LogUtil.error("⏱ TIMEOUT: " + getFullName(result));
        onTestFailure(result);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String getFullName(ITestResult result) {
        return result.getTestClass().getRealClass().getSimpleName()
                + "#" + result.getMethod().getMethodName();
    }

    private long getDuration(ITestResult result) {
        return result.getEndMillis() - result.getStartMillis();
    }
}
