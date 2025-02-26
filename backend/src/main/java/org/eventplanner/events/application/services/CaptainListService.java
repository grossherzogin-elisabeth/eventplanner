package org.eventplanner.events.application.services;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eventplanner.common.ObjectUtils;
import org.eventplanner.events.application.ports.PositionRepository;
import org.eventplanner.events.domain.entities.Event;
import org.eventplanner.events.domain.entities.Position;
import org.eventplanner.events.domain.entities.Registration;
import org.eventplanner.events.domain.entities.UserDetails;
import org.eventplanner.events.domain.entities.UserQualification;
import org.eventplanner.events.domain.values.PositionKey;
import org.eventplanner.events.domain.values.UserKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CaptainListService {

    private static final ZoneId timezone = ZoneId.of("Europe/Berlin");
    private final UserService userService;
    private final PositionRepository positionRepository;

    public CaptainListService(
        @Autowired UserService userService,
        @Autowired PositionRepository positionRepository
    ) {
        this.userService = userService;
        this.positionRepository = positionRepository;
    }

    public @NonNull ByteArrayOutputStream generateCaptainList(@NonNull Event event) throws IOException {
        var positionMap = new HashMap<PositionKey, Position>();
        positionRepository.findAll().forEach(position -> positionMap.put(position.getKey(), position));
        List<Registration> crewList = event.getAssignedRegistrations()
            .stream()
            .sorted((a, b) -> {
                // TODO get crew member name here for sorting
                // if (a.getPosition().equals(b.getPosition())) {
                //   return a.getName().compareTo(b.getName());
                // }
                var pa = positionMap.get(a.getPosition());
                var pb = positionMap.get(b.getPosition());
                return pb.getPriority() - pa.getPriority();
            })
            .toList();

        try (FileInputStream fileTemplate = new FileInputStream("data/templates/CaptainList_template.xlsx")) {
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);
            XSSFSheet sheet = workbook.getSheetAt(0);

            String currentDate = LocalDateTime.now().atZone(timezone).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            replacePlaceHolderInSheet(sheet, "{{creationDate}}", currentDate);

            writeCrewDataToSheet(sheet, crewList, positionMap);

            resizeSheet(sheet);

            return getWorkbookBytes(workbook);
        } catch (IOException e) {
            log.error("Failed to generate imo list workbook", e);
            throw e;
        }
    }

    private int findFirstEmptyRow(XSSFSheet sheet) {
        int rowCounter = -1;
        boolean rowIsEmpty = false;
        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext() && !rowIsEmpty) {
            rowCounter++;
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                if (cell != null && cell.getCellType() != CellType.BLANK) {
                    break;
                } else if (cell != null && cell.getCellType() == CellType.STRING && cell.getStringCellValue()
                    .trim()
                    .isEmpty()) {
                    break;
                } else if (!cellIterator.hasNext()) {
                    rowIsEmpty = true;
                }
            }
        }
        return rowCounter;
    }

    private void writeCrewDataToSheet(
        XSSFSheet sheet,
        List<Registration> crewList,
        Map<PositionKey, Position> positionMap
    ) throws IOException {

        int firstEmptyRow = findFirstEmptyRow(sheet);

        for (int crewCounter = 0; crewCounter < crewList.size(); crewCounter++) {
            Row currentRow = sheet.getRow(crewCounter + firstEmptyRow - 1);
            currentRow.getCell(0).setCellValue(crewCounter + 1);
            Registration currentRegistration = crewList.get(crewCounter);
            String imoListRank = ObjectUtils.mapNullable(
                positionMap.get(currentRegistration.getPosition()),
                Position::getImoListRank,
                currentRegistration.getPosition().value()
            );
            UserKey crewMemberKey = currentRegistration.getUserKey();
            if (crewMemberKey != null) {
                Optional<UserDetails> crewMemberDetails = userService.getUserByKey(crewMemberKey);

                if (crewMemberDetails.isPresent()) {
                    currentRow.getCell(1).setCellValue(crewMemberDetails.get().getLastName());
                    currentRow.getCell(2).setCellValue(crewMemberDetails.get().getFirstName());
                    currentRow.getCell(3).setCellValue(crewMemberDetails.get().getMobile());
                    currentRow.getCell(4).setCellValue(crewMemberDetails.get().getDiseases());
                    currentRow.getCell(5).setCellValue(crewMemberDetails.get().getMedication());
                    var diet = crewMemberDetails.get().getDiet();
                    if (diet != null) {
                        switch (diet.value()) {
                            case "vegetarian" -> currentRow.getCell(6).setCellValue("vegetarisch");
                            case "vegan" -> currentRow.getCell(6).setCellValue("vegan");
                        }
                    }
                    currentRow.getCell(7).setCellValue(crewMemberDetails.get().getIntolerances());
                    var emergencyContact = crewMemberDetails.get().getEmergencyContact();
                    if (emergencyContact != null) {
                        currentRow.getCell(8).setCellValue(emergencyContact.getName());
                        currentRow.getCell(9).setCellValue(emergencyContact.getPhone());
                    } else {
                        currentRow.getCell(8).setCellValue("");
                        currentRow.getCell(9).setCellValue("");
                    }
                    currentRow.getCell(10).setCellValue(imoListRank);

                    insertQualifications(currentRow, crewMemberDetails);

                    continue;
                }
            }
            // user not found
            currentRow.getCell(1).setCellValue(currentRegistration.getName());
            currentRow.getCell(2).setCellValue("");
            currentRow.getCell(3).setCellValue("");
            currentRow.getCell(4).setCellValue("");
            currentRow.getCell(6).setCellValue("");
            currentRow.getCell(8).setCellValue("");
            currentRow.getCell(9).setCellValue("");
        }
    }

    private void replacePlaceHolderInSheet(XSSFSheet sheet, String placeHolder, @NonNull String replacement) {
        Iterator<Row> rowIterator = sheet.iterator();
        int rowCounter = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            int cellCounter = 0;
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains(placeHolder)) {
                    sheet.getRow(rowCounter).getCell(cellCounter).setCellValue(replacement);
                }
                cellCounter++;
            }
            rowCounter++;
        }
    }

    private void insertQualifications(Row row, Optional<UserDetails> crewMemberDetails) {
        StringBuilder otherQualifications = new StringBuilder();
        List<Instant> expirationDateOtherQualifications = new ArrayList<>();
        StringBuilder radioQualifications = new StringBuilder();
        List<Instant> expirationDateRadioQualifications = new ArrayList<>();
        StringBuilder safetyQualifications = new StringBuilder();
        Set<Instant> expirationDateSafetyQualifications = new HashSet<>();
        var qualifications = crewMemberDetails.get().getQualifications();

        for (UserQualification qualification : qualifications) {
            String qualificationKeyValue = qualification.getQualificationKey().value();

            if ((qualificationKeyValue.contains("medical") || qualificationKeyValue.contains("aid"))
                & qualification.getExpiresAt() != null) {
                switch (qualificationKeyValue) {
                    case "medical-fitness" -> row.getCell(11).setCellValue(date2String(qualification.getExpiresAt()));
                    case "medical-care" -> row.getCell(18).setCellValue(date2String(qualification.getExpiresAt()));
                    case "first-aid" -> row.getCell(19).setCellValue(date2String(qualification.getExpiresAt()));
                }
            } else if (qualificationKeyValue.contains("funk")) {
                switch (qualificationKeyValue) {
                    case "funk-abz" -> radioQualifications.append("ABZ, ");
                    case "funk-goc" -> radioQualifications.append("GOC, ");
                    case "funk-lrc" -> radioQualifications.append("LRC, ");
                    case "funk-roc" -> radioQualifications.append("ROC, ");
                    case "funk-src" -> radioQualifications.append("SRC, ");
                }
                expirationDateRadioQualifications.add(qualification.getExpiresAt());
            } else if (qualificationKeyValue.contains("stcw-vi")) {
                if (safetyQualifications.isEmpty()) {
                    safetyQualifications.append("STCW VI/");
                }
                switch (qualificationKeyValue) {
                    case "stcw-vi-1" -> safetyQualifications.append("1, ");
                    case "stcw-vi-2" -> safetyQualifications.append("2, ");
                    case "stcw-vi-3" -> safetyQualifications.append("3, ");
                    case "stcw-vi-4" -> safetyQualifications.append("4, ");
                }
                expirationDateSafetyQualifications.add(qualification.getExpiresAt());
            } else {
                switch (qualificationKeyValue) {
                    case "asgt" -> otherQualifications.append("ASGT, ");
                    case "shs" -> otherQualifications.append("SHS, ");
                    case "sks" -> otherQualifications.append("SKS, ");
                    case "sss" -> otherQualifications.append("SSS, ");
                    case "tradi" -> otherQualifications.append("Tradi, ");
                    case "tradi-maschine" -> otherQualifications.append("Tradi-Masch, ");
                    case "masch-750" -> otherQualifications.append("Masch 750, ");
                    case "stcw-ii-1" -> otherQualifications.append("STCW II/1, ");
                    case "stcw-ii-2" -> otherQualifications.append("STCW II/2, ");
                    case "stcw-ii-3" -> otherQualifications.append("STCW II/3, ");
                    case "stcw-ii-4" -> otherQualifications.append("STCW II/4, ");
                    case "stcw-ii-5" -> otherQualifications.append("STCW II/5, ");
                    case "stcw-iii-1" -> otherQualifications.append("STCW III/1, ");
                    case "stcw-iii-2" -> otherQualifications.append("STCW III/2, ");
                    case "lissi-ma" -> otherQualifications.append("MA, ");
                    case "matrosenbrief" -> otherQualifications.append("MA-Brief, ");
                    case "lissi-lm" -> otherQualifications.append("LM, ");
                }
                expirationDateOtherQualifications.add(qualification.getExpiresAt());
            }
        }

        row.getCell(12).setCellValue(removeCommaAtStringEnd(otherQualifications.toString()));
        row.getCell(13).setCellValue(multipleDates2String(expirationDateOtherQualifications));
        row.getCell(14).setCellValue(removeCommaAtStringEnd(radioQualifications.toString()));
        row.getCell(15).setCellValue(multipleDates2String(expirationDateRadioQualifications));
        row.getCell(16).setCellValue(removeCommaAtStringEnd(safetyQualifications.toString()));
        row.getCell(17).setCellValue(multipleDates2String(new ArrayList<>(expirationDateSafetyQualifications)));
    }

    private String removeCommaAtStringEnd(String input) {
        return input.replaceAll(", $", "");
    }

    private String date2String(Instant date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return date.atZone(timezone).format(formatter);
    }

    private String multipleDates2String(List<Instant> dates) {
        String dateString = dates.stream()
            .filter(Objects::nonNull)
            .map(this::date2String)
            .collect(Collectors.joining(", "));
        return removeCommaAtStringEnd(dateString);
    }

    private void resizeSheet(XSSFSheet sheet) {
        int maxColumnWidth = 5000;
        int columnCount = sheet.getRow(0).getLastCellNum();
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 256);
            if (sheet.getColumnWidth(i) > maxColumnWidth) {
                sheet.setColumnWidth(i, maxColumnWidth);
            }
        }
        for (Row row : sheet)
            row.setHeight((short) -1);
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
