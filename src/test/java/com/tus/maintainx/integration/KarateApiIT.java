package com.tus.maintainx.integration;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class KarateApiIT {

    private static int port;

    @LocalServerPort
    void setPort(int localPort) {
        port = localPort;
    }

    @Karate.Test
    Karate api() {
        return Karate.run("classpath:karate/audit.feature", "classpath:karate/analytics.feature")
                .systemProperty("karate.baseUrl", "http://localhost:" + port);
    }
}
