package com.tus.maintainx.E2E;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class LoginFlowE2ETest extends AbstractSeleniumFlowTest {

    @Test
    void engineerCanLoginAndLogout() {
        saveUser("eng@mail.com", "ENGINEER", "eng123");

        openAppAndRequireUiAssets();
        login("eng@mail.com", "eng123");

        waitForVisible(org.openqa.selenium.By.id("engineerPage"));
        assertEquals("ENGINEER", executeScript("return sessionStorage.getItem('role');"));
        assertFalse(String.valueOf(executeScript("return sessionStorage.getItem('accessToken');")).isBlank());

        logout();

        waitForVisible(org.openqa.selenium.By.id("loginView"));
        waitForHidden(org.openqa.selenium.By.id("homeView"));
        assertNull(executeScript("return sessionStorage.getItem('accessToken');"));
    }
}
