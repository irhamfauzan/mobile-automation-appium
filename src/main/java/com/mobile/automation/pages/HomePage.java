package com.mobile.automation.pages;

import com.mobile.automation.core.BasePage;
import com.mobile.automation.utils.LogUtil;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

public class HomePage extends BasePage {

    private static final By HOME_SCREEN    = AppiumBy.accessibilityId("home-screen");
    private static final By PROFILE_ICON   = AppiumBy.accessibilityId("profile-icon");
    private static final By MENU_BUTTON    = AppiumBy.accessibilityId("menu-button");
    private static final By SEARCH_BAR     = AppiumBy.accessibilityId("search-bar");
    private static final By LOGOUT_BUTTON  = AppiumBy.accessibilityId("logout-button");
    private static final By WELCOME_TEXT   = By.xpath("//*[contains(@text,'Welcome') or contains(@text,'Home')]");
    private static final By DASHBOARD      = By.xpath("//android.view.View[@content-desc='dashboard']");

    @Override
    public boolean isPageLoaded() {
        return isDisplayed(HOME_SCREEN) || isDisplayed(WELCOME_TEXT) || isDisplayed(DASHBOARD);
    }

    public ProfilePage navigateToProfile() {
        LogUtil.info("Navigating to Profile");
        tap(PROFILE_ICON);
        return new ProfilePage();
    }

    public String getWelcomeMessage() {
        return getText(WELCOME_TEXT);
    }

    public HomePage openMenu() {
        LogUtil.info("Opening menu");
        tap(MENU_BUTTON);
        return this;
    }

    public HomePage search(String query) {
        LogUtil.info("Searching: " + query);
        tap(SEARCH_BAR);
        typeText(SEARCH_BAR, query);
        hideKeyboard();
        return this;
    }

    public LoginPage logout() {
        LogUtil.info("Logging out");
        tap(LOGOUT_BUTTON);
        return new LoginPage();
    }

    public boolean isLoggedIn() {
        return isDisplayed(PROFILE_ICON) || isDisplayed(HOME_SCREEN);
    }
}
