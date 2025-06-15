package org.eventplanner.events.application.services;

import static java.util.Optional.ofNullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eventplanner.events.application.ports.PositionRepository;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.entities.positions.Position;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final ZoneId timezone = ZoneId.of("Europe/Berlin");
    private final PositionRepository positionRepository;
    private final UserService userService;

    public @NonNull ByteArrayOutputStream exportEvents(@NonNull List<Event> events, int year) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Einsatzmatrix");
        sheet.setDefaultColumnWidth(18);

        var positions = new LinkedList<>(positionRepository.findAll());
        positions.sort((a, b) -> b.getPriority() - a.getPriority());
        var positionsByKey = new HashMap<PositionKey, Position>();
        var cellStyles = getCellStyles(workbook, positions);
        positions.forEach(position -> positionsByKey.put(position.getKey(), position));

        var r = 0;
        var yearCell = sheet.createRow(r++).createCell(0);
        yearCell.setCellValue(year);
        yearCell.setCellStyle(cellStyles.get("header"));
        sheet.createRow(r++);
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));

        var countCell = sheet.createRow(r++).createCell(0);
        countCell.setCellValue("Anzahl");
        countCell.setCellStyle(cellStyles.get("header"));

        var assignedRows = getRequiredRows(events, positions, false);
        for (var key : assignedRows) {
            var cell = sheet.createRow(r++).createCell(0);
            cell.setCellValue(positionsByKey.get(key).getName());
            cell.setCellStyle(cellStyles.get(key.value() + "-header"));
        }

        var waitingListCellStyle = workbook.createCellStyle();
        waitingListCellStyle.setAlignment(HorizontalAlignment.CENTER);
        waitingListCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        waitingListCellStyle.setBorderRight(BorderStyle.HAIR);
        waitingListCellStyle.setBorderBottom(BorderStyle.HAIR);
        var rowWaitingList = sheet.createRow(r++);
        for (int i = 0; i < events.size() + 1; i++) {
            var cell = rowWaitingList.createCell(i);
            cell.setCellValue("Warteliste");
            cell.setCellStyle(waitingListCellStyle);
        }

        var waitinglistRows = getRequiredRows(events, positions, true);
        for (var key : waitinglistRows) {
            var cell = sheet.createRow(r++).createCell(0);
            cell.setCellValue(positionsByKey.get(key).getName());
            cell.setCellStyle(cellStyles.get(key.value() + "-header"));
        }

        var users = new HashMap<UserKey, UserDetails>();
        userService.getDetailedUsers().forEach(u -> users.put(u.getKey(), u));
        for (int i = 0; i < events.size(); i++) {
            fillEvent(events.get(i), users, sheet, cellStyles, assignedRows, waitinglistRows, i + 1);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (bos) {
            workbook.write(bos);
            bos.flush();
        } catch (IOException e) {
            log.error("Failed to get workbook bytes", e);
        }
        return bos;
    }

    private @NonNull Map<String, XSSFCellStyle> getCellStyles(
        @NonNull XSSFWorkbook workbook,
        @NonNull List<Position> positions
    ) {
        Map<String, XSSFCellStyle> cellStyles = new HashMap<>();

        var fontBold = workbook.createFont();
        fontBold.setBold(true);
        fontBold.setFamily(FontFamily.MODERN);

        var styleColumnHeader = workbook.createCellStyle();
        styleColumnHeader.setAlignment(HorizontalAlignment.CENTER);
        styleColumnHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        styleColumnHeader.setBorderRight(BorderStyle.HAIR);
        styleColumnHeader.setBorderBottom(BorderStyle.HAIR);
        cellStyles.put("header", styleColumnHeader);

        for (Position position : positions) {
            int red = Integer.valueOf(position.getColor().substring(1, 3), 16);
            int green = Integer.valueOf(position.getColor().substring(3, 5), 16);
            int blue = Integer.valueOf(position.getColor().substring(5, 7), 16);
            var rgb = new byte[] { (byte) (red), (byte) (green), (byte) (blue) };
            var color = new XSSFColor(rgb, new DefaultIndexedColorMap());

            var styleNormal = workbook.createCellStyle();
            styleNormal.setAlignment(HorizontalAlignment.CENTER);
            styleNormal.setVerticalAlignment(VerticalAlignment.CENTER);
            styleNormal.setBorderRight(BorderStyle.HAIR);
            styleNormal.setBorderBottom(BorderStyle.HAIR);
            styleNormal.setFillForegroundColor(color);
            styleNormal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellStyles.put(position.getKey().value(), styleNormal);

            var styleRowHead = workbook.createCellStyle();
            styleRowHead.setAlignment(HorizontalAlignment.LEFT);
            styleRowHead.setVerticalAlignment(VerticalAlignment.CENTER);
            styleRowHead.setBorderRight(BorderStyle.HAIR);
            styleRowHead.setBorderBottom(BorderStyle.HAIR);
            styleRowHead.setFont(fontBold);
            styleNormal.setFillForegroundColor(color);
            styleNormal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellStyles.put(position.getKey().value() + "-header", styleNormal);
        }
        return cellStyles;
    }

    private @NonNull Map<PositionKey, Integer> getRequiredRowCount(@NonNull List<Event> events, boolean waitingList) {
        var positionCount = new HashMap<PositionKey, Integer>();
        for (Event event : events) {
            var assignedRegistrationKeys = event.getAssignedRegistrationKeys();
            var filteredRegistrationPositions = event.getRegistrations().stream()
                .filter(it -> !(waitingList && assignedRegistrationKeys.contains(it.getKey())))
                .map(Registration::getPosition)
                .toList();
            var positions = filteredRegistrationPositions.stream().distinct().toList();
            for (final PositionKey position : positions) {
                var count = (int) filteredRegistrationPositions.stream().filter(position::equals).count();
                if (positionCount.getOrDefault(position, 0) < count) {
                    positionCount.put(position, count);
                }
            }
        }
        return positionCount;
    }

    private @NonNull List<PositionKey> getRequiredRows(
        @NonNull List<Event> events,
        @NonNull List<Position> positions,
        boolean waitingList
    ) {
        var rowCountByPosition = getRequiredRowCount(events, waitingList);
        var rows = new LinkedList<PositionKey>();
        for (Position position : positions) {
            for (int i = 0; i < rowCountByPosition.getOrDefault(position.getKey(), 0); i++) {
                rows.add(position.getKey());
            }
        }
        return rows.stream().toList(); // make the list immutable
    }

    private void fillEvent(
        @NonNull Event event,
        @NonNull Map<UserKey, UserDetails> users,
        @NonNull XSSFSheet sheet,
        @NonNull Map<String, XSSFCellStyle> cellStyles,
        List<PositionKey> assignedRows,
        List<PositionKey> waitingListRows,
        int column
    ) {
        for (var it = sheet.rowIterator(); it.hasNext(); ) {
            var row = it.next();
            var cell = row.getCell(column, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            ofNullable(row.getCell(0))
                .map(Cell::getCellStyle)
                .ifPresent(cell::setCellStyle);
        }

        var headerStyle = cellStyles.get("header");
        sheet.getRow(0).getCell(column).setCellValue(event.getName());
        sheet.getRow(0).getCell(column).setCellStyle(headerStyle);
        sheet.getRow(1).getCell(column).setCellValue(
            event.getStart().atZone(timezone).format(DateTimeFormatter.ofPattern("dd.MM."))
                + " - " +
                event.getEnd().atZone(timezone).format(DateTimeFormatter.ofPattern("dd.MM."))
        );
        sheet.getRow(1).getCell(column).setCellStyle(headerStyle);

        sheet.getRow(2).getCell(column).setCellValue(event.getAssignedRegistrationKeys().size());
        sheet.getRow(2).getCell(column).setCellStyle(headerStyle);

        var assignedRegistrationKeys = event.getAssignedRegistrationKeys();
        for (var registration : event.getRegistrations()) {
            var row = 3;
            if (assignedRegistrationKeys.contains(registration.getKey())) {
                row += assignedRows.indexOf(registration.getPosition());
            } else {
                row += assignedRows.size() + 1;
                row += waitingListRows.indexOf(registration.getPosition());
            }
            var cell = sheet.getRow(row).getCell(column);
            while (cell.getRawValue() != null) {
                cell = sheet.getRow(row++).getCell(column);
            }
            var user = users.get(registration.getUserKey());
            if (user != null) {
                cell.setCellValue(user.getFullName());
            } else {
                cell.setCellValue(registration.getName());
            }
        }
    }
}
