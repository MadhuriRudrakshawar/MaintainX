package com.tus.maintainx.karate;

import com.intuit.karate.junit5.Karate;
import com.tus.maintainx.MaintainXApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(classes = MaintainXApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KarateRunnerTestIT {

    private static int port;

    @LocalServerPort
    void setPort(int localPort) {
        port = localPort;
    }

    @Karate.Test
    Karate api() {
        return Karate.run(
                        "classpath:com/tus/maintainx/karate/audit.feature",
                        "classpath:com/tus/maintainx/karate/analytics.feature"
                )
                .systemProperty("karate.baseUrl", "http://localhost:" + port);
    }
}
