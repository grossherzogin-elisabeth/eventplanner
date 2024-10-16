package org.eventplanner;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Disabled("Figure out why SQL Exception throws on test startup")
@SpringBootTest
@ActiveProfiles(profiles = "local, test")
class ApplicationTests {

    @Test
    void contextLoads() {
    }

}
