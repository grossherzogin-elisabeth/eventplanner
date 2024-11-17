package org.eventplanner.events.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.values.RegistrationKey;
import org.eventplanner.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
public class ConsumptionListService {

    private final UserService userService;

    public ConsumptionListService(@Autowired UserService userService) {
        this.userService = userService;
    }

    public @NonNull ByteArrayOutputStream generateConsumptionList(@NonNull Event event) throws IOException {
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
            writeCrewDataToWorkbook(workbook, getNameList(crewList));

            return getWorkbookBytes(workbook);
        } catch (IOException e) {
            log.error("Failed to generate consumption list workbook", e);
            throw e;
        }
    }

    private List<String> getNameList(List<Registration> crewList) {
        var userNames = crewList.stream()
                .map(Registration::getUser)
                .filter(Objects::nonNull)
                .map(userService::getUserByKey)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(user ->  user.getNickName() != null
                            ? user.getNickName()+ "\n" + user.getLastName()
                            : user.getFirstName()+ "\n" + user.getLastName());

        var guestNames = crewList.stream()
                .filter(registration -> registration.getUser() == null)
                .map(Registration::getName)
                .filter(Objects::nonNull)
                .map(s -> s.contains(",")
                        ? s.substring(s.indexOf(",") + 1).trim() + "\n" + s.substring(0, s.indexOf(","))
                        : s.replaceFirst("\\s", "\n"));

        return Stream.concat(userNames, guestNames).sorted().toList();
    }

    private void writeCrewDataToWorkbook(XSSFWorkbook workbook, List<String> nameList) {
        XSSFSheet sheet = workbook.getSheetAt(0);
        for (int i = 0; i < nameList.size(); i++) {
            sheet.getRow(i * 2).getCell(0).setCellValue(nameList.get(i));
        }
    }

    private ByteArrayOutputStream getWorkbookBytes(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (bos) {
            workbook.write(bos);
            bos.flush();
        } catch (IOException e) {
            log.error("Failed to get workbook bytes", e);
        }
        return bos;
    }
}
