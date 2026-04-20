package com.mobile.automation.listeners;

import com.mobile.automation.utils.LogUtil;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries flaky tests up to MAX_RETRY times before marking as failed.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final int MAX_RETRY = 2;
    private final ThreadLocal<Integer> retryCount = ThreadLocal.withInitial(() -> 0);

    @Override
    public boolean retry(ITestResult result) {
        int current = retryCount.get();
        if (current < MAX_RETRY) {
            retryCount.set(current + 1);
            LogUtil.warn("Retrying test [" + (current + 1) + "/" + MAX_RETRY + "]: "
                    + result.getMethod().getMethodName());
            return true;
        }
        retryCount.set(0);
        return false;
    }
}
