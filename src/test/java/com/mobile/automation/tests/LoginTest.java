package com.mobile.automation.tests;

import com.mobile.automation.base.BaseTest;
import com.mobile.automation.pages.ForgotPasswordPage;
import com.mobile.automation.pages.HomePage;
import com.mobile.automation.pages.LoginPage;
import com.mobile.automation.utils.ExcelReader;
import com.mobile.automation.utils.JsonReader;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * LoginTest covers 5 scenarios: happy path, invalid credentials,
 * empty fields, forgot-password navigation, and data-driven login.
 */
public class LoginTest extends BaseTest {

    private static final String EXCEL_DATA = "src/test/resources/testdata/login_data.xlsx";
    private static final String JSON_DATA  = "src/test/resources/testdata/login_data.json";

    // ─── Smoke ───────────────────────────────────────────────────────────────

    @Test(groups = {"smoke", "regression"},
          description = "Verify successful login with valid credentials")
    public void testSuccessfulLogin() {
        logStep("Navigate to login screen");
        LoginPage loginPage = new LoginPage();
        Assert.assertTrue(loginPage.isPageLoaded(), "Login page should be loaded");

        logStep("Enter valid credentials and tap login");
        HomePage homePage = loginPage.loginWith(
                config.getProperty("valid.username", "testuser@example.com"),
                config.getProperty("valid.password", "Test@1234")
        );

        logStep("Verify home page is displayed");
        Assert.assertTrue(homePage.isPageLoaded(), "Home page should load after successful login");
        Assert.assertTrue(homePage.isLoggedIn(), "User should be logged in");

        logInfo("Successful login verified");
    }

    // ─── Regression ──────────────────────────────────────────────────────────

    @Test(groups = {"smoke", "regression"},
          description = "Verify error message shown for invalid credentials")
    public void testInvalidCredentials() {
        logStep("Attempt login with invalid credentials");
        LoginPage loginPage = new LoginPage()
                .attemptLoginWith("invalid@example.com", "WrongPass123");

        logStep("Verify error message is displayed");
        Assert.assertTrue(loginPage.isErrorDisplayed(), "Error message should be displayed");

        String errorMsg = loginPage.getErrorMessage();
        Assert.assertNotNull(errorMsg, "Error message text should not be null");
        Assert.assertFalse(errorMsg.isEmpty(), "Error message should not be empty");

        logInfo("Error message displayed: " + errorMsg);
    }

    @Test(groups = {"regression"},
          description = "Verify login button state with empty fields")
    public void testEmptyFieldsValidation() {
        logStep("Open login page with empty fields");
        LoginPage loginPage = new LoginPage();
        Assert.assertTrue(loginPage.isPageLoaded(), "Login page should be loaded");

        logStep("Attempt login without entering credentials");
        loginPage.tapLoginExpectingError();

        logStep("Verify error is shown or login button is disabled");
        boolean hasError = loginPage.isErrorDisplayed();
        boolean isDisabled = !loginPage.isLoginButtonEnabled();
        Assert.assertTrue(hasError || isDisabled,
                "App should show error or disable login when fields are empty");

        logInfo("Empty field validation: error=" + hasError + ", disabled=" + isDisabled);
    }

    @Test(groups = {"regression"},
          description = "Verify navigation to Forgot Password page")
    public void testForgotPasswordNavigation() {
        logStep("Open login page");
        LoginPage loginPage = new LoginPage();
        Assert.assertTrue(loginPage.isPageLoaded(), "Login page should be loaded");

        logStep("Tap Forgot Password link");
        ForgotPasswordPage forgotPage = loginPage.tapForgotPassword();

        logStep("Verify Forgot Password page loaded");
        Assert.assertTrue(forgotPage.isPageLoaded(), "Forgot Password page should be loaded");

        logStep("Submit reset request with valid email");
        forgotPage.requestPasswordReset("testuser@example.com");

        logStep("Verify success or error message");
        boolean hasSuccess = forgotPage.isSuccessMessageDisplayed();
        boolean hasError = forgotPage.isErrorMessageDisplayed();
        Assert.assertTrue(hasSuccess || hasError, "A confirmation or error message should appear");

        logInfo("Forgot password flow verified");
    }

    // ─── Data-driven ─────────────────────────────────────────────────────────

    @Test(groups = {"regression", "data-driven"},
          description = "Data-driven login test using JSON test data",
          dataProvider = "loginDataJson")
    public void testLoginWithJsonData(Map<String, Object> testData) {
        String username = (String) testData.get("username");
        String password = (String) testData.get("password");
        boolean expectSuccess = Boolean.parseBoolean(testData.get("expectSuccess").toString());
        String scenario = (String) testData.getOrDefault("scenario", "unnamed");

        logStep("Scenario: " + scenario + " | User: " + username);
        logStep("Expected outcome: " + (expectSuccess ? "SUCCESS" : "FAILURE"));

        LoginPage loginPage = new LoginPage();
        Assert.assertTrue(loginPage.isPageLoaded(), "Login page should be loaded");

        if (expectSuccess) {
            HomePage homePage = loginPage.loginWith(username, password);
            Assert.assertTrue(homePage.isPageLoaded(),
                    "Scenario '" + scenario + "': Home page should load");
        } else {
            loginPage.attemptLoginWith(username, password);
            Assert.assertTrue(loginPage.isErrorDisplayed(),
                    "Scenario '" + scenario + "': Error should be displayed");
        }
    }

    @Test(groups = {"regression", "data-driven"},
          description = "Data-driven login test using Excel test data",
          dataProvider = "loginDataExcel")
    public void testLoginWithExcelData(Map<String, String> testData) {
        String username = testData.get("username");
        String password = testData.get("password");
        boolean expectSuccess = "true".equalsIgnoreCase(testData.get("expectSuccess"));
        String scenario = testData.getOrDefault("scenario", "unnamed");

        logStep("Excel scenario: " + scenario + " | User: " + username);

        LoginPage loginPage = new LoginPage();
        Assert.assertTrue(loginPage.isPageLoaded(), "Login page should be loaded");

        if (expectSuccess) {
            HomePage homePage = loginPage.loginWith(username, password);
            Assert.assertTrue(homePage.isPageLoaded(),
                    "Excel scenario '" + scenario + "': Home page should load");
        } else {
            loginPage.attemptLoginWith(username, password);
            Assert.assertTrue(loginPage.isErrorDisplayed(),
                    "Excel scenario '" + scenario + "': Error should be displayed");
        }
    }

    // ─── Data providers ──────────────────────────────────────────────────────

    @DataProvider(name = "loginDataJson", parallel = false)
    public Object[][] loginDataJson() {
        return JsonReader.getTestData(JSON_DATA, "loginTests");
    }

    @DataProvider(name = "loginDataExcel", parallel = false)
    public Object[][] loginDataExcel() {
        return ExcelReader.getTestDataAsMaps(EXCEL_DATA, "LoginData");
    }
}
