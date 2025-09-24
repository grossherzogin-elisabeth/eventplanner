package org.eventplanner.integration.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.eventplanner.events.application.usecases.EventExportUseCase;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.testdata.SignedInUserFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@AutoConfigureMockMvc
@Transactional // resets db changes after each test
class EventExportIntegrationTest {

    @Autowired
    private EventExportUseCase testee;

    @Test
    void shouldGenerateCaptainList() throws Exception {
        var user = SignedInUserFactory.createSignedInUser().withRole(Role.ADMIN);
        var out = testee.exportCaptainList(user, new EventKey("98b55fb5-7f10-42c7-9b94-2e5c9b92f264"));
        var target = new File("captain-list.out.xlsx");
        try (OutputStream outputStream = new FileOutputStream(target)) {
            out.writeTo(outputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        FileUtils.delete(target);
    }
}
