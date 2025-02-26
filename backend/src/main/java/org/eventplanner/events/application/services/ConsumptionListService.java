package org.eventplanner.events.application.services;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eventplanner.events.domain.entities.Event;
import org.eventplanner.events.domain.entities.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConsumptionListService {

    private final UserService userService;

    public ConsumptionListService(@Autowired UserService userService) {
        this.userService = userService;
    }

    public @NonNull ByteArrayOutputStream generateConsumptionList(@NonNull Event event) throws IOException {
        List<Registration> crewList = event.getAssignedRegistrations();
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
            .map(Registration::getUserKey)
            .filter(Objects::nonNull)
            .map(userService::getUserByKey)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(user -> user.getNickName() != null
                ? user.getNickName() + "\n" + user.getLastName()
                : user.getFirstName() + "\n" + user.getLastName());

        var guestNames = crewList.stream()
            .filter(registration -> registration.getUserKey() == null)
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
            sheet.getRow(((i + 1) * 2 - 1)).getCell(0).setCellValue(nameList.get(i));
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
