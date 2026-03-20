package com.tus.maintainx.E2E;

import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalyticsDashboardFlowE2ETest extends AbstractSeleniumFlowTest {

    @Test
    void adminCanOpenAnalyticsDashboard() {
        UserEntity admin = saveUser("admin@mail.com", "ADMIN", "admin123");
        UserEntity engineer = saveUser("eng@mail.com", "ENGINEER", "eng123");
        NetworkElementEntity core = saveNetworkElement("NE-301", "Analytics Core", "CORE_ROUTER", "DUBLIN", "ACTIVE");
        NetworkElementEntity edge = saveNetworkElement("NE-302", "Analytics Edge", "EDGE_SWITCH", "CORK", "DEACTIVE");
        saveMaintenanceWindow(
                "Approved Analytics Window",
                "APPROVED",
                engineer,
                "appr@mail.com",
                LocalDateTime.now().plusDays(3).withHour(1).withMinute(0).withSecond(0).withNano(0),
                LocalDateTime.now().plusDays(3).withHour(3).withMinute(0).withSecond(0).withNano(0),
                core,
                edge
        );

        openAppAndRequireUiAssets();
        login(admin.getUsername(), "admin123");

        waitForVisible(org.openqa.selenium.By.id("adminPage"));
        driver.findElement(org.openqa.selenium.By.cssSelector(".js-view-analytics")).click();

        waitForVisible(org.openqa.selenium.By.id("analyticsView"));
        waitForText(org.openqa.selenium.By.id("analyticsView"), "Analytics Dashboard");
        waitForCondition(d -> Boolean.TRUE.equals(executeScript(
                "return !!Chart.getChart('chartElementsByType') && !!Chart.getChart('chartElementHealth') && !!Chart.getChart('chartMaintenanceStatus');"
        )));
        waitForCondition(d -> Boolean.TRUE.equals(executeScript(
                "return !!document.getElementById('chartApprovedWindowSchedule');"
        )));

        assertEquals("Back to Dashboard", driver.findElement(org.openqa.selenium.By.id("backToDashboardBtn")).getText().trim());
    }
}
