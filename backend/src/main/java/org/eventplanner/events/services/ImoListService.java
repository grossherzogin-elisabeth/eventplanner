package org.eventplanner.events.services;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.values.RegistrationKey;
import org.eventplanner.users.entities.User;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.service.UserService;
import org.eventplanner.users.values.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ImoListService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final UserService userService;

    public ImoListService(@Autowired UserService userService) {
        this.userService = userService;
    }

    public @NonNull File generateImoList(@NonNull Event event) throws IOException {
//        var file = File.createTempFile(event.getKey() + "-imo-list", "txt");
//        Files.writeString(file.toPath(), "Hello world");
//        file.deleteOnExit();

        List<RegistrationKey> assignedRegistrationsKeys = event.getSlots().stream()
                .map(Slot::getAssignedRegistration)
                .filter(Objects::nonNull)
                .toList();

        List<Registration> crewList = event.getRegistrations()
                .stream()
                .filter(registration -> assignedRegistrationsKeys.contains(registration.getKey()))
                .toList();

        FileInputStream fileTemplate = new FileInputStream("data/templates/ImoList_template.xlsx");
        var outputFile = File.createTempFile(event.getKey() + "-imo-list", "xlsx");
        FileOutputStream out = new FileOutputStream(outputFile);

        try (fileTemplate; out) {
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);
            XSSFSheet sheet = workbook.getSheetAt(0);
            int crewSize = crewList.size();
            int firstEmptyRow = findFirstEmptyRow(sheet);

            for (int crewCounter = 1; crewCounter < crewSize; crewCounter++){
                UserKey crewMemberKey = crewList.get(crewCounter).getUser();

                Row currentRow = sheet.getRow(crewCounter+firstEmptyRow-1);
                currentRow.getCell(0).setCellValue(crewCounter);
                if (crewMemberKey != null) {
                    Optional<UserDetails> crewMemberDetails = userService.getUserByKey(Objects.requireNonNull(crewMemberKey));

                    if (crewMemberDetails.isPresent()) {
                        currentRow.getCell(1).setCellValue(crewMemberDetails.get().getLastName());
                        currentRow.getCell(2).setCellValue(crewMemberDetails.get().getFirstName());
                        currentRow.getCell(3).setCellValue(crewList.get(crewCounter).getPosition().value());
                        currentRow.getCell(4).setCellValue(crewMemberDetails.get().getNationality());
                        currentRow.getCell(5).setCellValue(Objects.requireNonNull(crewMemberDetails.get().getDateOfBirth()).toLocalDateTime());
                        currentRow.getCell(6).setCellValue(crewMemberDetails.get().getPlaceOfBirth());
                        currentRow.getCell(7).setCellValue(crewMemberDetails.get().getPassNr());
                    }
                }
                else {
                    // found no user for the given user key
                    currentRow.getCell(2).setCellValue("");
                    currentRow.getCell(3).setCellValue("");
                    currentRow.getCell(4).setCellValue(crewList.get(crewCounter).getPosition().value());
                    currentRow.getCell(5).setCellValue("");
                    currentRow.getCell(6).setCellValue("");
                    currentRow.getCell(7).setCellValue("");
                    currentRow.getCell(8).setCellValue("");
                }
                // TODO Gastcrew
            }

            workbook.write(out);
        }
        catch (IOException e) {
            log.error("Failed to save temporary users import file", e);
        }



        // TODO generate the excel
        return outputFile;
    }

    private int findFirstEmptyRow(XSSFSheet sheet) {
        int rowCounter = -1;
        boolean rowIsEmpty = false;
        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext() && !rowIsEmpty) {
            rowCounter++;
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext() && !rowIsEmpty) {
                Cell cell = cellIterator.next();
                if (cell != null && cell.getCellType() != CellType.BLANK) {
                    break;
                } else if (cell != null && cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty()) {
                    break;
                } else if (!cellIterator.hasNext()) {
                    rowIsEmpty = true;
                }
            }
        }
        return rowCounter;
    }
}
