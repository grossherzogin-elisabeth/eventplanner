package org.eventplanner.events.services;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.eventplanner.common.ObjectUtils;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.values.RegistrationKey;
import org.eventplanner.positions.adapter.PositionRepository;
import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.service.UserService;
import org.eventplanner.users.values.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ImoListService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
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

        List<RegistrationKey> assignedRegistrationsKeys = event.getSlots().stream()
                .map(Slot::getAssignedRegistration)
                .filter(Objects::nonNull)
                .toList();

        List<Registration> crewList = event.getRegistrations()
                .stream()
                .filter(registration -> assignedRegistrationsKeys.contains(registration.getKey()))
                .toList();

        FileInputStream fileTemplate = new FileInputStream("data/templates/ImoList_template.xlsx");

        try (fileTemplate) {
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);
            XSSFSheet sheet = workbook.getSheetAt(0);

            String arrivalPort = event.getLocations().getFirst().name();
            String departurePort = event.getLocations().getLast().name();
            addEventDetailsToImoList(sheet,"{{Port_a/d}}",arrivalPort + "/" + departurePort);

            String arrivalDate = event.getStart().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            String departureDate = event.getEnd().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            addEventDetailsToImoList(sheet,"{{Date_a/d}}",arrivalDate + "/" + departureDate);

            writeCrewDataToSheet(sheet, crewList, positionMap);

            return getWorkbookBytes(workbook);
        }catch (IOException e) {
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
                } else if (cell != null && cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty()) {
                    break;
                } else if (!cellIterator.hasNext()) {
                    rowIsEmpty = true;
                }
            }
        }
        return rowCounter;
    }

    private void writeCrewDataToSheet(XSSFSheet sheet, List<Registration> crewList, Map<PositionKey, Position> positionMap) throws IOException{

        int firstEmptyRow = findFirstEmptyRow(sheet);

        for (int crewCounter = 1; crewCounter < crewList.size(); crewCounter++){
            Row currentRow = sheet.getRow(crewCounter+firstEmptyRow-1);
            currentRow.getCell(0).setCellValue(crewCounter);
            Registration currentRegistration = crewList.get(crewCounter);
            String imoListRank = ObjectUtils.mapNullable(positionMap.get(currentRegistration.getPosition()),
                    Position::getImoListRank,
                    currentRegistration.getPosition().value());
            UserKey crewMemberKey = currentRegistration.getUser();
            if (crewMemberKey != null) {
                Optional<UserDetails> crewMemberDetails = userService.getUserByKey(crewMemberKey);

                if (crewMemberDetails.isPresent()) {
                    currentRow.getCell(1).setCellValue(crewMemberDetails.get().getLastName());
                    currentRow.getCell(2).setCellValue(crewMemberDetails.get().getFirstName());
                    currentRow.getCell(3).setCellValue(imoListRank);
                    currentRow.getCell(4).setCellValue(crewMemberDetails.get().getNationality());
                    var dateOfBirth = crewMemberDetails.get().getDateOfBirth();
                    if (dateOfBirth != null) {
                        currentRow.getCell(5).setCellValue(dateOfBirth.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    } else {
                        currentRow.getCell(5).setCellValue("");
                    }
                    currentRow.getCell(6).setCellValue(crewMemberDetails.get().getPlaceOfBirth());
                    currentRow.getCell(7).setCellValue(crewMemberDetails.get().getPassNr());
                    continue;
                }
            }
            // user not found
            currentRow.getCell(1).setCellValue(currentRegistration.getName());
            currentRow.getCell(2).setCellValue("");
            currentRow.getCell(3).setCellValue(imoListRank);
            currentRow.getCell(4).setCellValue("");
            currentRow.getCell(5).setCellValue("");
            currentRow.getCell(6).setCellValue("");
            currentRow.getCell(7).setCellValue("");
        }
    }

    private void addEventDetailsToImoList(XSSFSheet sheet,String placeHolder, String eventDetail){
        Iterator<Row> rowIterator = sheet.iterator();
        int rowCounter = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            int cellCounter = 0;
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains(placeHolder)) {
                    sheet.getRow(rowCounter).getCell(cellCounter).setCellValue(Objects.requireNonNullElse(eventDetail, ""));
                }
                cellCounter++;
            }
            rowCounter++;
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
