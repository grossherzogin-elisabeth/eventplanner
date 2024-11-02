package org.eventplanner.events.services;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.service.UserService;
import org.eventplanner.users.values.UserKey;
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

    private final UserService userService;

    public ImoListService(@Autowired UserService userService) {
        this.userService = userService;
    }

    public @NonNull File generateImoList(@NonNull Event event) throws IOException {
//        var file = File.createTempFile(event.getKey() + "-imo-list", "txt");
//        Files.writeString(file.toPath(), "Hello world");
//        file.deleteOnExit();

        List<Registration> crewList = event.getRegistrations();

        FileInputStream fileTemplate = new FileInputStream(new File("data/templates/ImoList_template.xlsx"));
        var outputFile = File.createTempFile(event.getKey() + "-imo-list", "xlsx");
        FileOutputStream out = new FileOutputStream(outputFile);

        try (fileTemplate; out) {
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            int crewCounter = 0;
            while (rowIterator.hasNext() && crewCounter < event.getRegistrations().size()) {
                Row row = rowIterator.next();
                if (!isRowEmpty(row)) {
                    continue;
                }
                UserKey crewMemberKey = crewList.get(crewCounter).getUser();
                Optional<UserDetails> crewMemberDetails = userService.getUserByKey(crewMemberKey);

                row.getCell(1).setCellValue(crewCounter + 1);
                row.getCell(2).setCellValue(crewMemberDetails.get().getLastName());
                row.getCell(3).setCellValue(crewMemberDetails.get().getFirstName());
                row.getCell(4).setCellValue(crewList.get(crewCounter).getPosition().value());
                row.getCell(5).setCellValue(crewMemberDetails.get().getNationality());
                row.getCell(6).setCellValue(Objects.requireNonNull(crewMemberDetails.get().getDateOfBirth()).toLocalDateTime());
                row.getCell(7).setCellValue(crewMemberDetails.get().getPlaceOfBirth());
                row.getCell(8).setCellValue(crewMemberDetails.get().getPassNr());

                crewCounter++;
            }

            workbook.write(out);
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }



        // TODO generate the excel
        return outputFile;
    }

    private boolean isRowEmpty(Row row) {
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            if (cell != null && cell.getCellType() != CellType.BLANK){
                return false;
            } else if (cell != null && cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
