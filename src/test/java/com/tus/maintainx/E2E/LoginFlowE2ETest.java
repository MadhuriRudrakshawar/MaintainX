package com.tus.maintainx.E2E;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LoginFlowE2ETest extends AbstractSeleniumFlowTest {

    @Test
    void engineerCanLoginAndLogout() {
        saveUser("eng", "ENGINEER", "eng123");

        openAppAndRequireUiAssets();
        login("eng", "eng123");

        waitForVisible(org.openqa.selenium.By.id("engineerPage"));
        assertEquals("ENGINEER", executeScript("return sessionStorage.getItem('role');"));
        assertFalse(String.valueOf(executeScript("return sessionStorage.getItem('accessToken');")).isBlank());

        logout();

        waitForVisible(org.openqa.selenium.By.id("loginView"));
        waitForHidden(org.openqa.selenium.By.id("homeView"));
        assertEquals(null, executeScript("return sessionStorage.getItem('accessToken');"));
    }
}
