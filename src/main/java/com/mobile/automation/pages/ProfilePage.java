package com.mobile.automation.pages;

import com.mobile.automation.core.BasePage;
import com.mobile.automation.utils.LogUtil;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

public class ProfilePage extends BasePage {

    private static final By PROFILE_HEADER    = AppiumBy.accessibilityId("profile-header");
    private static final By USERNAME_LABEL    = AppiumBy.accessibilityId("profile-username");
    private static final By EMAIL_LABEL       = AppiumBy.accessibilityId("profile-email");
    private static final By EDIT_BUTTON       = AppiumBy.accessibilityId("edit-profile");
    private static final By SAVE_BUTTON       = AppiumBy.accessibilityId("save-profile");
    private static final By BACK_BUTTON       = AppiumBy.accessibilityId("back-button");
    private static final By AVATAR            = AppiumBy.accessibilityId("profile-avatar");
    private static final By CHANGE_PASSWORD   = AppiumBy.accessibilityId("change-password");

    @Override
    public boolean isPageLoaded() {
        return isDisplayed(PROFILE_HEADER) || isDisplayed(USERNAME_LABEL);
    }

    public String getDisplayedUsername() {
        return getText(USERNAME_LABEL);
    }

    public String getDisplayedEmail() {
        return getText(EMAIL_LABEL);
    }

    public ProfilePage tapEdit() {
        LogUtil.info("Tapping Edit Profile");
        tap(EDIT_BUTTON);
        return this;
    }

    public ProfilePage tapSave() {
        LogUtil.info("Saving profile");
        tap(SAVE_BUTTON);
        return this;
    }

    public ProfilePage updateUsername(String newUsername) {
        LogUtil.info("Updating username to: " + newUsername);
        tapEdit();
        clearAndType(USERNAME_LABEL, newUsername);
        tapSave();
        return this;
    }

    public ForgotPasswordPage tapChangePassword() {
        LogUtil.info("Tapping Change Password");
        tap(CHANGE_PASSWORD);
        return new ForgotPasswordPage();
    }

    public HomePage goBack() {
        tap(BACK_BUTTON);
        return new HomePage();
    }

    public boolean isAvatarDisplayed() {
        return isDisplayed(AVATAR);
    }
}
