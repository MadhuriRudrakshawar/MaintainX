package com.tus.maintainx.E2E;

import com.tus.maintainx.entity.NetworkElementEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminNetworkElementFlowE2ETest extends AbstractSeleniumFlowTest {

    @Test
    void adminCanCreateNetworkElementFromDashboard() {
        saveUser("admin@mail.com", "ADMIN", "admin123");
        saveNetworkElement("NE-001", "Existing Core Router", "CORE_ROUTER", "DUBLIN", "ACTIVE");

        openAppAndRequireUiAssets();
        login("admin@mail.com", "admin123");

        waitForVisible(org.openqa.selenium.By.id("adminPage"));
        waitForText(org.openqa.selenium.By.cssSelector("#neTable tbody"), "NE-001");

        driver.findElement(org.openqa.selenium.By.id("showAddElementBtn")).click();
        waitForVisible(org.openqa.selenium.By.id("addElementPanel"));

        driver.findElement(org.openqa.selenium.By.id("neName")).sendKeys("Admin Flow Element");
        driver.findElement(org.openqa.selenium.By.id("neType")).sendKeys("Core Router");
        driver.findElement(org.openqa.selenium.By.id("neRegion")).sendKeys("Dublin");
        driver.findElement(org.openqa.selenium.By.id("saveElementBtn")).click();

        waitForCondition(d -> networkElementRepository.findAll().stream()
                .anyMatch(ne -> "Admin Flow Element".equals(ne.getName())));

        NetworkElementEntity created = networkElementRepository.findAll().stream()
                .filter(ne -> "Admin Flow Element".equals(ne.getName()))
                .findFirst()
                .orElseThrow();

        waitForText(org.openqa.selenium.By.cssSelector("#neTable tbody"), created.getElementCode());
        waitForText(org.openqa.selenium.By.cssSelector("#neTable tbody"), "Admin Flow Element");
        assertTrue(created.getElementCode().matches("NE-\\d{3}"));
        assertEquals("Admin Flow Element", created.getName());
    }
}
