package org.eventplanner.events.services;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.values.RegistrationKey;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.service.UserService;
import org.eventplanner.users.values.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ConsumptionListService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final UserService userService;

    public ConsumptionListService(@Autowired UserService userService) {
        this.userService = userService;
    }

    public @NonNull byte[] generateConsumptionList(@NonNull Event event) throws IOException {

        List<RegistrationKey> assignedRegistrationsKeys = event.getSlots().stream()
                .map(Slot::getAssignedRegistration)
                .filter(Objects::nonNull)
                .toList();

        List<Registration> crewList = event.getRegistrations()
                .stream()
                .filter(registration -> assignedRegistrationsKeys.contains(registration.getKey()))
                .toList();

        FileInputStream fileTemplate = new FileInputStream("data/templates/ConsumptionList_template.xlsx");

        try (fileTemplate) {
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);
            XSSFSheet sheet = workbook.getSheetAt(0);
            int crewSize = crewList.size();

            for (int crewCounter = 1; crewCounter < crewSize; crewCounter++){
                UserKey crewMemberKey = crewList.get(crewCounter).getUser();

                if (crewMemberKey != null) {
                    Optional<UserDetails> crewMemberDetails = userService.getUserByKey(Objects.requireNonNull(crewMemberKey));

                    if (crewMemberDetails.isPresent()) {
                        sheet.getRow(crewCounter*2-1).getCell(0).setCellValue(crewMemberDetails.get().getLastName());
                        sheet.getRow(crewCounter*2).getCell(0).setCellValue(crewMemberDetails.get().getFirstName());
                    }
                }
                else {
                    // found no user for the given user key
                    sheet.getRow(crewCounter*2-1).getCell(0).setCellValue("");
                    sheet.getRow(crewCounter*2).getCell(0).setCellValue("");
                }
                // TODO Gastcrew
            }

            return getWorkbookBytes(workbook);
        }
        catch (IOException e) {
            log.error("Failed to generate consumption list workbook", e);
        }

        return null;
    }

    private byte[] getWorkbookBytes(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try(bos){
            workbook.write(bos);
            bos.flush();
        }
        catch (IOException e) {
            log.error("Failed to get workbook bytes", e);
        }
        return bos.toByteArray();
    }
}
