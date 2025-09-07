package org.eventplanner.events.application.services;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eventplanner.common.ObjectUtils;
import org.eventplanner.events.application.ports.PositionRepository;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.entities.positions.Position;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.values.events.EventLocation;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ImoListService {

    private static final ZoneId timezone = ZoneId.of("Europe/Berlin");
    private final UserService userService;
    private final PositionRepository positionRepository;

    public ImoListService(
        @Autowired UserService userService,
        @Autowired PositionRepository positionRepository
    ) {
        this.userService = userService;
        this.positionRepository = positionRepository;
    }

    public @NonNull ByteArrayOutputStream generateImoList(@NonNull Event event) throws IOException {
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

        try (FileInputStream fileTemplate = new FileInputStream("data/templates/ImoList_template.xlsx")) {
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);
            XSSFSheet sheet = workbook.getSheetAt(0);

            if (!event.getLocations().isEmpty()) {
                String departurePort = event.getLocations().stream().findFirst().map(EventLocation::name).orElse("");
                replacePlaceHolderInSheet(sheet, "{{Port_a/d}}", departurePort);
            } else {
                replacePlaceHolderInSheet(sheet, "{{Port_a/d}}", "");
            }
            String departureDate = event.getStart().atZone(timezone).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            replacePlaceHolderInSheet(sheet, "{{Date_a/d}}", departureDate);

            writeCrewDataToSheet(sheet, crewList, positionMap);

            return getWorkbookBytes(workbook);
        } catch (IOException e) {
            log.error("Failed to generate imo list workbook", e);
            throw e;
        }
    }

    private int findFirstEmptyRow(@NonNull XSSFSheet sheet) {
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
        @NonNull XSSFSheet sheet,
        @NonNull List<Registration> crewList,
        @NonNull Map<PositionKey, Position> positionMap
    ) throws IOException {

        int firstEmptyRow = findFirstEmptyRow(sheet);

        for (int crewCounter = 0; crewCounter < crewList.size(); crewCounter++) {
            Row currentRow = sheet.getRow(crewCounter + firstEmptyRow);
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
                    currentRow.getCell(3).setCellValue(imoListRank);
                    currentRow.getCell(4).setCellValue(crewMemberDetails.get().getNationality());
                    var dateOfBirth = crewMemberDetails.get().getDateOfBirth();
                    if (dateOfBirth != null) {
                        currentRow.getCell(6)
                            .setCellValue(dateOfBirth.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    } else {
                        currentRow.getCell(6).setCellValue("");
                    }
                    currentRow.getCell(8).setCellValue(crewMemberDetails.get().getPlaceOfBirth());
                    currentRow.getCell(9).setCellValue(crewMemberDetails.get().getPassNr());
                    continue;
                }
            }
            // user not found
            currentRow.getCell(1).setCellValue(currentRegistration.getName());
            currentRow.getCell(2).setCellValue("");
            currentRow.getCell(3).setCellValue(imoListRank);
            currentRow.getCell(4).setCellValue("");
            currentRow.getCell(6).setCellValue("");
            currentRow.getCell(8).setCellValue("");
            currentRow.getCell(9).setCellValue("");
        }
    }

    private void replacePlaceHolderInSheet(
        @NonNull XSSFSheet sheet,
        @NonNull String placeHolder,
        @NonNull String replacement
    ) {
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

    private @NonNull ByteArrayOutputStream getWorkbookBytes(@NonNull XSSFWorkbook workbook) throws IOException {
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
