package org.eventplanner.events.services;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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
import java.util.*;

@Service
public class ConsumptionListService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
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

            List<String> nameList = getNameList(crewList);
            Collections.sort(nameList);
            writeCrewDataToWorkbook(workbook, nameList);

            return getWorkbookBytes(workbook);
        }
        catch (IOException e) {
            log.error("Failed to generate consumption list workbook", e);
            throw e;
        }
    }

    private List<String> getNameList(List<Registration> crewList){

        List<String> nameList = new ArrayList<>();

        for (Registration registration : crewList) {

            UserKey crewMemberKey = registration.getUser();
            if (crewMemberKey != null) {
                Optional<UserDetails> crewMemberDetails = userService.getUserByKey(Objects.requireNonNull(crewMemberKey));
                crewMemberDetails.ifPresent(userDetails -> nameList.add(userDetails.getFirstName() + "\n" + userDetails.getLastName()));
            } else {
                String guestName = Objects.requireNonNull(registration.getName());
                if (guestName.contains(",")) {
                    String guestLastName = guestName.substring(0, guestName.indexOf(","));
                    String guestFirstName = guestName.substring(guestName.indexOf(",") + 1).trim();
                    nameList.add(guestFirstName + "\n" + guestLastName);
                } else {
                    nameList.add(guestName.replaceFirst("\\s", "\n"));
                }
            }
        }
        return nameList;
    }

    private void writeCrewDataToWorkbook(XSSFWorkbook workbook, List<String> nameList) throws IOException{

        XSSFSheet sheet = workbook.getSheetAt(0);
        for (int i = 1;i< nameList.size(); i++){
            sheet.getRow(i*2-1).getCell(0).setCellValue(nameList.get(i));
        }
    }

    private ByteArrayOutputStream getWorkbookBytes(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try(bos){
            workbook.write(bos);
            bos.flush();
        }
        catch (IOException e) {
            log.error("Failed to get workbook bytes", e);
        }
        return bos;
    }
}
