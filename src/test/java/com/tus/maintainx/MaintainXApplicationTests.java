package com.tus.maintainx;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

@SpringBootTest(classes = MaintainXApplication.class)
class MaintainXApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void main_runsSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            MaintainXApplication.main(new String[0]);

            springApplication.verify(() ->
                    SpringApplication.run(MaintainXApplication.class, new String[0]));
        }
    }

}
