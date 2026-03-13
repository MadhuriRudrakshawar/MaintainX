package com.tus.maintainx.E2E;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class EngineerMaintenanceWindowFlowE2ETest extends AbstractSeleniumFlowTest {

    @Test
    void engineerCanBookMaintenanceWindow() {
        saveUser("eng", "ENGINEER", "eng123");
        saveNetworkElement("NE-101", "Booking Candidate A", "CORE_ROUTER", "DUBLIN", "ACTIVE");
        saveNetworkElement("NE-102", "Booking Candidate B", "EDGE_SWITCH", "CORK", "ACTIVE");

        openAppAndRequireUiAssets();
        login("eng", "eng123");

        waitForVisible(org.openqa.selenium.By.id("engineerPage"));
        waitForCondition(d -> !d.findElements(org.openqa.selenium.By.cssSelector("#mwElements input[type='checkbox']")).isEmpty());

        driver.findElement(org.openqa.selenium.By.id("showAddWindowBtn")).click();
        waitForVisible(org.openqa.selenium.By.id("addWindowPanel"));

        driver.findElement(org.openqa.selenium.By.id("mwTitle")).sendKeys("Engineer Flow Window");
        setDateTimeValue("mwStart", LocalDateTime.now().plusDays(1).withSecond(0).withNano(0));
        setDateTimeValue("mwEnd", LocalDateTime.now().plusDays(1).plusHours(2).withSecond(0).withNano(0));

        List<org.openqa.selenium.WebElement> checkboxes = driver.findElements(org.openqa.selenium.By.cssSelector("#mwElements input[type='checkbox']"));
        assertFalse(checkboxes.isEmpty());
        checkboxes.get(0).click();

        driver.findElement(org.openqa.selenium.By.id("saveWindowBtn")).click();

        waitForText(org.openqa.selenium.By.cssSelector("#mwTable tbody"), "Engineer Flow Window");
        waitForText(org.openqa.selenium.By.cssSelector("#mwTable tbody"), "PENDING");
        assertFalse(maintenanceWindowRepository.findAll().stream().noneMatch(mw -> "Engineer Flow Window".equals(mw.getTitle())));
    }
}
