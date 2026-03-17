package com.tus.maintainx.E2E;

import com.tus.maintainx.MaintainXApplication;
import com.tus.maintainx.entity.MaintenanceWindowEntity;
import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.entity.UserEntity;
import com.tus.maintainx.enums.AuditAction;
import com.tus.maintainx.enums.AuditEntityType;
import com.tus.maintainx.enums.ExecutionStatus;
import com.tus.maintainx.repository.AuditLogRepository;
import com.tus.maintainx.repository.MaintenanceWindowRepository;
import com.tus.maintainx.repository.NetworkElementRepository;
import com.tus.maintainx.repository.UserRepository;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;

@SpringBootTest(classes = MaintainXApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractSeleniumFlowTest {

    protected static final DateTimeFormatter DATE_TIME_INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @LocalServerPort
    protected int port;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected NetworkElementRepository networkElementRepository;

    @Autowired
    protected MaintenanceWindowRepository maintenanceWindowRepository;

    @Autowired
    protected AuditLogRepository auditLogRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected WebDriver driver;
    protected WebDriverWait wait;

    @RegisterExtension
    final TestWatcher screenshotOnFailure = new TestWatcher() {
        @Override
        @NullMarked
        public void testFailed(ExtensionContext context, @Nullable Throwable cause) {
            captureFailureScreenshot(context);
        }
    };

    @BeforeEach
    void setUp(TestInfo testInfo) {
        resetDatabase();
        driver = createDriverOrSkip(testInfo);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void openAppAndRequireUiAssets() {
        driver.get(baseUrl());
        wait.until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));

        try {
            wait.until(d -> Boolean.TRUE.equals(((JavascriptExecutor) d).executeScript(
                    "return !!window.jQuery && !!window.bootstrap && typeof $.fn.DataTable === 'function' && !!window.Chart;"
            )));
        } catch (TimeoutException ex) {
            Assumptions.assumeTrue(false,
                    "UI dependencies did not load. Selenium flow tests require browser access to the CDN-hosted assets used by index.html.");
        }
    }

    protected void login(String username, String password) {
        waitForVisible(By.id("loginView"));
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("loginBtn")).click();
    }

    protected void logout() {
        driver.findElement(By.id("logoutBtn")).click();
    }

    protected void setDateTimeValue(String elementId, LocalDateTime value) {
        WebElement input = driver.findElement(By.id(elementId));
        ((JavascriptExecutor) driver).executeScript(
                """
                        arguments[0].value = arguments[1];
                        arguments[0].dispatchEvent(new Event('input', { bubbles: true }));
                        arguments[0].dispatchEvent(new Event('change', { bubbles: true }));
                        """,
                input,
                value.format(DATE_TIME_INPUT)
        );
    }

    protected void acceptAlert() {
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        alert.accept();
    }

    protected void waitForVisible(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void waitForHidden(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected void waitForText(By locator, String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    protected void waitForCondition(java.util.function.Function<WebDriver, Boolean> condition) {
        wait.until(condition);
    }

    protected Object executeScript(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    protected UserEntity saveUser(String username, String role, String password) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    protected NetworkElementEntity saveNetworkElement(String code, String name, String type, String region, String status) {
        NetworkElementEntity element = new NetworkElementEntity();
        element.setElementCode(code);
        element.setName(name);
        element.setElementType(type);
        element.setRegion(region);
        element.setStatus(status);
        return networkElementRepository.save(element);
    }

    protected MaintenanceWindowEntity saveMaintenanceWindow(
            String title,
            String status,
            UserEntity requester,
            String decidedBy,
            LocalDateTime start,
            LocalDateTime end,
            NetworkElementEntity... elements
    ) {
        MaintenanceWindowEntity window = MaintenanceWindowEntity.builder()
                .title(title)
                .description(title + " description")
                .startTime(start)
                .endTime(end)
                .windowStatus(status)
                .rejectionReason("REJECTED".equalsIgnoreCase(status) ? "Seeded rejection reason" : null)
                .decidedBy(decidedBy)
                .executionStatus(ExecutionStatus.PLANNED)
                .requestedBy(requester)
                .networkElements(new HashSet<>(Arrays.asList(elements)))
                .build();
        return maintenanceWindowRepository.save(window);
    }

    protected void saveAuditLog(
            AuditEntityType entityType,
            Long entityId,
            AuditAction action,
            String username,
            String roleName,
            String details,
            LocalDateTime createdAt
    ) {
        auditLogRepository.save(com.tus.maintainx.entity.AuditLogEntity.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .username(username)
                .roleName(roleName)
                .details(details)
                .createdAt(createdAt)
                .build());
    }

    protected String baseUrl() {
        return "http://localhost:" + port + "/";
    }

    private void captureFailureScreenshot(ExtensionContext context) {
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            return;
        }

        File screenshot = screenshotDriver.getScreenshotAs(OutputType.FILE);
        File destination = new File("target/screenshots/" + screenshotFileName(context) + ".png");

        try {
            FileUtils.copyFile(screenshot, destination);
        } catch (IOException ignored) {
            // Avoid masking the original test failure if screenshot persistence fails.
        }
    }

    private String screenshotFileName(ExtensionContext context) {
        String className = context.getRequiredTestClass().getSimpleName();
        String methodName = context.getRequiredTestMethod().getName();
        return (className + "-" + methodName).replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private WebDriver createDriverOrSkip(TestInfo testInfo) {
        try {
            return new ChromeDriver(chromeOptions());
        } catch (WebDriverException chromeFailure) {
            try {
                return new EdgeDriver(edgeOptions());
            } catch (WebDriverException edgeFailure) {
                Assumptions.assumeTrue(false,
                        "No usable Chrome or Edge WebDriver was available for Selenium flow test '" + testInfo.getDisplayName() + "'.");
                throw edgeFailure;
            }
        }
    }

    private ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--window-size=1600,1200", "--disable-gpu", "--no-sandbox");
        return options;
    }

    private EdgeOptions edgeOptions() {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--headless=new", "--window-size=1600,1200", "--disable-gpu", "--no-sandbox");
        return options;
    }

    private void resetDatabase() {
        maintenanceWindowRepository.deleteAll();
        auditLogRepository.deleteAll();
        networkElementRepository.deleteAll();
        userRepository.deleteAll();
    }
}
