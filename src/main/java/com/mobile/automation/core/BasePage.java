package com.mobile.automation.core;

import com.mobile.automation.utils.LogUtil;
import com.mobile.automation.utils.ScreenshotUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Base class providing reusable mobile actions for all Page Objects.
 * Uses explicit waits for reliability and abstracts platform differences.
 */
public abstract class BasePage {

    protected AppiumDriver driver;
    protected WebDriverWait wait;
    protected WebDriverWait shortWait;

    private static final int DEFAULT_TIMEOUT = 15;
    private static final int SHORT_TIMEOUT = 5;
    private static final int SWIPE_DURATION = 800;

    protected BasePage() {
        this.driver = DriverManager.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(SHORT_TIMEOUT));
    }

    // ─── Element interactions ────────────────────────────────────────────────

    protected WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void tap(By locator) {
        LogUtil.debug("Tapping: " + locator);
        waitForClickable(locator).click();
    }

    protected void tap(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
    }

    protected void typeText(By locator, String text) {
        LogUtil.debug("Typing '" + text + "' into: " + locator);
        WebElement element = waitForElement(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected void clearAndType(By locator, String text) {
        WebElement element = waitForElement(locator);
        element.clear();
        hideKeyboard();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return waitForElement(locator).getText();
    }

    protected String getAttribute(By locator, String attribute) {
        return waitForElement(locator).getAttribute(attribute);
    }

    protected boolean isDisplayed(By locator) {
        try {
            return shortWait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (TimeoutException | NoSuchElementException e) {
            return false;
        }
    }

    protected boolean isEnabled(By locator) {
        try {
            return waitForElement(locator).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean elementExists(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected List<WebElement> findElements(By locator) {
        return driver.findElements(locator);
    }

    // ─── Keyboard ────────────────────────────────────────────────────────────

    protected void hideKeyboard() {
        try {
            if (driver instanceof AndroidDriver) {
                ((AndroidDriver) driver).hideKeyboard();
            } else if (driver instanceof IOSDriver) {
                ((IOSDriver) driver).hideKeyboard();
            }
        } catch (Exception e) {
            LogUtil.debug("Keyboard not present or already hidden");
        }
    }

    protected void pressEnter(By locator) {
        waitForElement(locator).sendKeys(Keys.ENTER);
    }

    // ─── Scrolling & Swiping ─────────────────────────────────────────────────

    protected void scrollDown() {
        Dimension size = driver.manage().window().getSize();
        int startX = size.width / 2;
        int startY = (int) (size.height * 0.8);
        int endY = (int) (size.height * 0.2);
        swipe(startX, startY, startX, endY);
    }

    protected void scrollUp() {
        Dimension size = driver.manage().window().getSize();
        int startX = size.width / 2;
        int startY = (int) (size.height * 0.2);
        int endY = (int) (size.height * 0.8);
        swipe(startX, startY, startX, endY);
    }

    protected void swipeLeft() {
        Dimension size = driver.manage().window().getSize();
        int startX = (int) (size.width * 0.8);
        int endX = (int) (size.width * 0.2);
        int y = size.height / 2;
        swipe(startX, y, endX, y);
    }

    protected void swipeRight() {
        Dimension size = driver.manage().window().getSize();
        int startX = (int) (size.width * 0.2);
        int endX = (int) (size.width * 0.8);
        int y = size.height / 2;
        swipe(startX, y, endX, y);
    }

    private void swipe(int startX, int startY, int endX, int endY) {
        org.openqa.selenium.interactions.PointerInput finger =
                new org.openqa.selenium.interactions.PointerInput(
                        org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
        org.openqa.selenium.interactions.Sequence swipeSeq = new org.openqa.selenium.interactions.Sequence(finger, 1);

        swipeSeq.addAction(finger.createPointerMove(Duration.ZERO,
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), startX, startY));
        swipeSeq.addAction(finger.createPointerDown(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        swipeSeq.addAction(finger.createPointerMove(Duration.ofMillis(SWIPE_DURATION),
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), endX, endY));
        swipeSeq.addAction(finger.createPointerUp(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(List.of(swipeSeq));
    }

    protected void scrollToElement(By locator) {
        int maxScrolls = 10;
        for (int i = 0; i < maxScrolls; i++) {
            if (isDisplayed(locator)) return;
            scrollDown();
        }
        throw new NoSuchElementException("Element not found after " + maxScrolls + " scrolls: " + locator);
    }

    // ─── Waits ───────────────────────────────────────────────────────────────

    protected void waitForElementToDisappear(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected void waitForText(By locator, String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    protected void waitSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ─── App control ─────────────────────────────────────────────────────────

    protected void launchApp() {
        String appId = getAppId();
        ((InteractsWithApps) driver).activateApp(appId);
    }

    protected void closeApp() {
        String appId = getAppId();
        ((InteractsWithApps) driver).terminateApp(appId);
    }

    protected void resetApp() {
        closeApp();
        launchApp();
    }

    protected void background(int seconds) {
        ((InteractsWithApps) driver).runAppInBackground(Duration.ofSeconds(seconds));
    }

    private String getAppId() {
        Object pkg = driver.getCapabilities().getCapability("appium:appPackage");
        if (pkg != null) return pkg.toString();
        Object bundle = driver.getCapabilities().getCapability("appium:bundleId");
        if (bundle != null) return bundle.toString();
        throw new IllegalStateException("No appPackage or bundleId capability set");
    }

    // ─── Screenshot helper ───────────────────────────────────────────────────

    protected String captureScreenshot(String name) {
        return ScreenshotUtil.capture(driver, name);
    }

    // ─── Abstract contract ───────────────────────────────────────────────────

    public abstract boolean isPageLoaded();
}
