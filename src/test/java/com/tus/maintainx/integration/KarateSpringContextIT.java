package com.tus.maintainx.integration;

import com.tus.maintainx.MaintainXApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@SpringBootTest(classes = MaintainXApplication.class)
@ActiveProfiles("test")
class KarateSpringContextIT {

    @Autowired
    private KarateSpringContext karateSpringContext;

    @Test
    void exposesSeedDataAndTokensForKarateFeatures() {
        assertSame(karateSpringContext, KarateSpringContext.getInstance());

        Map<String, Object> seed = karateSpringContext.seedBaselineData();

        assertEquals("admin@mail.com", seed.get("adminUsername"));
        assertEquals("approver1@mail.com", seed.get("approverUsername"));
        assertEquals("engineer1@mail.com", seed.get("engineerUsername"));
        assertNotNull(seed.get("approvedWindowId"));
        assertNotNull(karateSpringContext.tokenFor("admin@mail.com"));
    }
}
