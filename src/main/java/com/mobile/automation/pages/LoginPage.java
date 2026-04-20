package com.mobile.automation.pages;

import com.mobile.automation.core.BasePage;
import com.mobile.automation.utils.LogUtil;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Login page — demonstrates fluent API pattern for method chaining.
 * All locators use accessibility IDs first (cross-platform), with XPath fallbacks.
 */
public class LoginPage extends BasePage {

    // ─── Locators ────────────────────────────────────────────────────────────

    // Prefer accessibility ID for cross-platform compatibility
    private static final By USERNAME_FIELD     = AppiumBy.accessibilityId("username");
    private static final By PASSWORD_FIELD     = AppiumBy.accessibilityId("password");
    private static final By LOGIN_BUTTON       = AppiumBy.accessibilityId("login-button");
    private static final By FORGOT_PASSWORD    = AppiumBy.accessibilityId("forgot-password");
    private static final By SIGN_UP_LINK       = AppiumBy.accessibilityId("sign-up");
    private static final By ERROR_MESSAGE      = AppiumBy.accessibilityId("error-message");
    private static final By LOADING_INDICATOR  = AppiumBy.accessibilityId("loading-indicator");

    // XPath fallbacks (adjust to match your actual app)
    private static final By USERNAME_FIELD_XP  = By.xpath("//android.widget.EditText[@hint='Username' or @hint='Email']");
    private static final By PASSWORD_FIELD_XP  = By.xpath("//android.widget.EditText[@hint='Password']");
    private static final By LOGIN_BUTTON_XP    = By.xpath("//android.widget.Button[@text='Login' or @text='Sign In']");
    private static final By ERROR_MESSAGE_XP   = By.xpath("//*[contains(@text,'Invalid') or contains(@text,'Error') or contains(@text,'incorrect')]");

    // ─── Fluent API ──────────────────────────────────────────────────────────

    public LoginPage enterUsername(String username) {
        LogUtil.debug("Entering username: " + username);
        By locator = elementExists(USERNAME_FIELD) ? USERNAME_FIELD : USERNAME_FIELD_XP;
        typeText(locator, username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        LogUtil.debug("Entering password");
        By locator = elementExists(PASSWORD_FIELD) ? PASSWORD_FIELD : PASSWORD_FIELD_XP;
        typeText(locator, password);
        hideKeyboard();
        return this;
    }

    public HomePage tapLogin() {
        LogUtil.info("Tapping Login button");
        By locator = elementExists(LOGIN_BUTTON) ? LOGIN_BUTTON : LOGIN_BUTTON_XP;
        tap(locator);
        waitForLoadingToFinish();
        return new HomePage();
    }

    public LoginPage tapLoginExpectingError() {
        LogUtil.info("Tapping Login button (expecting error)");
        By locator = elementExists(LOGIN_BUTTON) ? LOGIN_BUTTON : LOGIN_BUTTON_XP;
        tap(locator);
        waitForLoadingToFinish();
        return this;
    }

    public ForgotPasswordPage tapForgotPassword() {
        LogUtil.info("Tapping Forgot Password");
        tap(FORGOT_PASSWORD);
        return new ForgotPasswordPage();
    }

    // ─── Compound actions ────────────────────────────────────────────────────

    public HomePage loginWith(String username, String password) {
        return enterUsername(username)
                .enterPassword(password)
                .tapLogin();
    }

    public LoginPage attemptLoginWith(String username, String password) {
        return enterUsername(username)
                .enterPassword(password)
                .tapLoginExpectingError();
    }

    // ─── Assertions & state ──────────────────────────────────────────────────

    @Override
    public boolean isPageLoaded() {
        return isDisplayed(USERNAME_FIELD) || isDisplayed(USERNAME_FIELD_XP);
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(ERROR_MESSAGE) || isDisplayed(ERROR_MESSAGE_XP);
    }

    public String getErrorMessage() {
        By locator = elementExists(ERROR_MESSAGE) ? ERROR_MESSAGE : ERROR_MESSAGE_XP;
        return getText(locator);
    }

    public boolean isLoginButtonEnabled() {
        By locator = elementExists(LOGIN_BUTTON) ? LOGIN_BUTTON : LOGIN_BUTTON_XP;
        return isEnabled(locator);
    }

    public boolean isUsernameFieldEmpty() {
        By locator = elementExists(USERNAME_FIELD) ? USERNAME_FIELD : USERNAME_FIELD_XP;
        String value = getAttribute(locator, "text");
        return value == null || value.isEmpty();
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private void waitForLoadingToFinish() {
        try {
            if (isDisplayed(LOADING_INDICATOR)) {
                waitForElementToDisappear(LOADING_INDICATOR);
            }
        } catch (Exception e) {
            LogUtil.debug("No loading indicator found");
        }
    }
}
