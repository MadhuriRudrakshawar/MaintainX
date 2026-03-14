package com.tus.maintainx.E2E;

import com.tus.maintainx.enums.AuditAction;
import com.tus.maintainx.enums.AuditEntityType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditLogFlowE2ETest extends AbstractSeleniumFlowTest {

    @Test
    void adminCanOpenAuditLogAndSeeRecentEntries() {
        saveUser("admin@mail.com", "ADMIN", "admin123");
        saveAuditLog(
                AuditEntityType.NETWORK_ELEMENT,
                1L,
                AuditAction.CREATED,
                "admin@mail.com",
                "ADMIN",
                "Network element created from audit flow seed",
                LocalDateTime.now().minusMinutes(5)
        );
        saveAuditLog(
                AuditEntityType.MAINTENANCE_WINDOW,
                10L,
                AuditAction.APPROVED,
                "appr@mail.com",
                "APPROVER",
                "Maintenance window approved from audit flow seed",
                LocalDateTime.now().minusMinutes(2)
        );

        openAppAndRequireUiAssets();
        login("admin@mail.com", "admin123");

        waitForVisible(org.openqa.selenium.By.id("adminPage"));
        driver.findElement(org.openqa.selenium.By.id("adminAuditBtn")).click();

        waitForVisible(org.openqa.selenium.By.id("auditModal"));
        waitForText(org.openqa.selenium.By.cssSelector("#auditTable tbody"), "Maintenance window approved from audit flow seed");
        waitForText(org.openqa.selenium.By.cssSelector("#auditTable tbody"), "Network element created from audit flow seed");

        String tableText = driver.findElement(org.openqa.selenium.By.cssSelector("#auditTable tbody")).getText();
        assertTrue(tableText.contains("APPROVED"));
        assertTrue(tableText.contains("CREATED"));
    }
}
