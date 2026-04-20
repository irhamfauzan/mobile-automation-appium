package com.mobile.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Centralized logging using Log4j2. Caller class name is resolved automatically.
 */
public class LogUtil {

    private LogUtil() {}

    private static Logger getLogger() {
        // Walk the stack to find the actual calling class (skip LogUtil itself)
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        String callerClass = LogUtil.class.getName();
        for (StackTraceElement element : stack) {
            String cls = element.getClassName();
            if (!cls.equals(Thread.class.getName()) && !cls.equals(callerClass)) {
                return LogManager.getLogger(cls);
            }
        }
        return LogManager.getLogger(LogUtil.class);
    }

    public static void info(String message) {
        getLogger().info(message);
    }

    public static void debug(String message) {
        getLogger().debug(message);
    }

    public static void warn(String message) {
        getLogger().warn(message);
    }

    public static void error(String message) {
        getLogger().error(message);
    }

    public static void error(String message, Throwable throwable) {
        getLogger().error(message, throwable);
    }

    public static void fatal(String message) {
        getLogger().fatal(message);
    }

    public static void trace(String message) {
        getLogger().trace(message);
    }
}
