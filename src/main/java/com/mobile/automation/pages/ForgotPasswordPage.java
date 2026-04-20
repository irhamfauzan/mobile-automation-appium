package com.mobile.automation.pages;

import com.mobile.automation.core.BasePage;
import com.mobile.automation.utils.LogUtil;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

public class ForgotPasswordPage extends BasePage {

    private static final By PAGE_HEADER       = AppiumBy.accessibilityId("forgot-password-header");
    private static final By EMAIL_FIELD       = AppiumBy.accessibilityId("reset-email");
    private static final By SUBMIT_BUTTON     = AppiumBy.accessibilityId("reset-submit");
    private static final By SUCCESS_MESSAGE   = AppiumBy.accessibilityId("reset-success");
    private static final By ERROR_MESSAGE     = AppiumBy.accessibilityId("reset-error");
    private static final By BACK_TO_LOGIN     = AppiumBy.accessibilityId("back-to-login");
    private static final By EMAIL_FIELD_XP    = By.xpath("//android.widget.EditText[@hint='Email' or @hint='Enter your email']");

    @Override
    public boolean isPageLoaded() {
        return isDisplayed(PAGE_HEADER) || isDisplayed(EMAIL_FIELD) || isDisplayed(EMAIL_FIELD_XP);
    }

    public ForgotPasswordPage enterEmail(String email) {
        LogUtil.info("Entering reset email: " + email);
        By locator = elementExists(EMAIL_FIELD) ? EMAIL_FIELD : EMAIL_FIELD_XP;
        typeText(locator, email);
        hideKeyboard();
        return this;
    }

    public ForgotPasswordPage submitResetRequest() {
        LogUtil.info("Submitting password reset request");
        tap(SUBMIT_BUTTON);
        return this;
    }

    public ForgotPasswordPage requestPasswordReset(String email) {
        return enterEmail(email).submitResetRequest();
    }

    public boolean isSuccessMessageDisplayed() {
        return isDisplayed(SUCCESS_MESSAGE);
    }

    public boolean isErrorMessageDisplayed() {
        return isDisplayed(ERROR_MESSAGE);
    }

    public String getSuccessMessage() {
        return getText(SUCCESS_MESSAGE);
    }

    public String getErrorMessage() {
        return getText(ERROR_MESSAGE);
    }

    public LoginPage tapBackToLogin() {
        LogUtil.info("Returning to Login page");
        tap(BACK_TO_LOGIN);
        return new LoginPage();
    }
}
