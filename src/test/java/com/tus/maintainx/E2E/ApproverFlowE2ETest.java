package com.tus.maintainx.E2E;

import com.tus.maintainx.entity.MaintenanceWindowEntity;
import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApproverFlowE2ETest extends AbstractSeleniumFlowTest {

    @Test
    void approverCanApprovePendingMaintenanceWindow() {
        UserEntity approver = saveUser("appr@mail.com", "APPROVER", "apr123");
        UserEntity engineer = saveUser("eng@mail.com", "ENGINEER", "eng123");
        NetworkElementEntity element = saveNetworkElement("NE-201", "Approver Flow Element", "CORE_ROUTER", "DUBLIN", "ACTIVE");
        MaintenanceWindowEntity window = saveMaintenanceWindow(
                "Pending Approval Window",
                "PENDING",
                engineer,
                "PENDING",
                LocalDateTime.now().plusDays(2).withHour(1).withMinute(0).withSecond(0).withNano(0),
                LocalDateTime.now().plusDays(2).withHour(3).withMinute(0).withSecond(0).withNano(0),
                element
        );

        openAppAndRequireUiAssets();
        login(approver.getUsername(), "apr123");

        waitForVisible(org.openqa.selenium.By.id("approverPage"));
        waitForText(org.openqa.selenium.By.id("pendingMwCards"), "Pending Approval Window");

        driver.findElement(org.openqa.selenium.By.cssSelector("#pendingMwCards .js-approve")).click();
        acceptAlert();

        waitForCondition(d -> maintenanceWindowRepository.findById(window.getId())
                .map(mw -> "APPROVED".equalsIgnoreCase(mw.getWindowStatus()))
                .orElse(false));
        waitForCondition(d -> !d.findElement(org.openqa.selenium.By.id("pendingMwCards")).getText().contains("Pending Approval Window"));

        MaintenanceWindowEntity updated = maintenanceWindowRepository.findById(window.getId()).orElseThrow();
        assertEquals("APPROVED", updated.getWindowStatus());
        assertEquals("appr@mail.com", updated.getDecidedBy());
        assertTrue(updated.getRejectionReason() == null || updated.getRejectionReason().isBlank());
    }
}
