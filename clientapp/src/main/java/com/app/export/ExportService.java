package com.app.export;


import com.app.model.Airport;
import com.app.model.Flight;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

/**
 * экспорт списка полетов в excel файл с разбивкой по листам по аэропортам вылета
 * @author Root
 */
public class ExportService extends Service<String> {


    LocalDate from;
    LocalDate to;
    List<Flight> flights;



    public void startService(List<Flight> flights, LocalDate from,LocalDate to) {

        if (!isRunning()) {
            this.flights = flights;
            this.from = from;
            this.to = to;
            reset();
            start();
        }

    }

    public boolean stopService() {

        if (isRunning()) {
            return cancel();
        }
        return false;
    }





    @Override
    protected Task createTask() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {
                // имя файла
                String fileName = String.format("/report%s_%s.xlsx", from.format(DateTimeFormatter.ofPattern("yyyyMMdd")), to.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                // директория  записи
                String userHome = System.getProperty("user.dir").concat("/report");
                // создадим если нет
                new File(userHome).mkdir();

                // создадим workbook
                XSSFWorkbook workbook = new XSSFWorkbook();
                XSSFCellStyle style = workbook.createCellStyle();
                style.setBorderBottom(BorderStyle.THIN);
                style.setBorderTop(BorderStyle.THIN);
                style.setBorderLeft(BorderStyle.THIN);
                style.setBorderRight(BorderStyle.THIN);
                style.setWrapText(true);

                // шрифт
                XSSFFont font = workbook.createFont();
                font.setFontHeightInPoints((short) 10);
                font.setFontName("Arial");
                font.setBold(true);
                style.setFont(font);

                // стили
                XSSFCellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setBorderBottom(BorderStyle.THIN);
                cellStyle.setBorderTop(BorderStyle.THIN);
                cellStyle.setBorderLeft(BorderStyle.THIN);
                cellStyle.setBorderRight(BorderStyle.THIN);

                // стиль заголовка
                XSSFCellStyle headStyle = workbook.createCellStyle();
                headStyle.setBorderBottom(BorderStyle.THIN);
                headStyle.setBorderTop(BorderStyle.THIN);
                headStyle.setBorderLeft(BorderStyle.THIN);
                headStyle.setBorderRight(BorderStyle.THIN);
                headStyle.setWrapText(true);
                headStyle.setAlignment(HorizontalAlignment.CENTER);
                headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                headStyle.setFont(font);

                int ind = 0;
                int i = 0;
                this.updateProgress(i, flights.size());
                updateMessage("Начало экспорта");
                // сгруппируем список рейсов по аэропортам
                Map<Airport,List<Flight>> map = flights.stream().collect(groupingBy(Flight::getAreaSrc));
                // "пройдемся" по списку и сохраним каждый аэропорт на отдельном листе
                for (Map.Entry<Airport, List<Flight>> entry : map.entrySet()) {
                    int rowCount = 0;
                    Airport airport = entry.getKey();
                    updateMessage(String.format("Формирую  данные по аэропорту %s", airport.getCode()));
                    XSSFSheet sheet = workbook.createSheet(String.format("AIRPORT %s",airport.getCode()));
                    ind++;
                    XSSFRow row = sheet.createRow(rowCount);
                    sheet.addMergedRegion(new CellRangeAddress(rowCount, rowCount, 0, 13));
                    XSSFCell cellHeader = row.createCell(0);
                    cellHeader.setCellType(CellType.STRING);

                    XSSFCellStyle s = workbook.createCellStyle();
                    s.setFont(font);
                    s.setAlignment(HorizontalAlignment.CENTER);


                    cellHeader.setCellValue(String.format("Список авиарейсов по аэропорту  %s,%s,%s", airport.getCode(),airport.getCity(),
                            airport.getCountry().getName()));
                    cellHeader.setCellStyle(s);
                    rowCount++;
                    row = sheet.createRow(rowCount);
                    sheet.addMergedRegion(new CellRangeAddress(rowCount, rowCount, 0, 13));
                    cellHeader = row.createCell(0);
                    cellHeader.setCellType(CellType.STRING);
                    cellHeader.setCellValue(
                            String.format("За период с %s по %s",
                                    from.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                    to.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));

                    cellHeader.setCellStyle(s);

                    rowCount++;
                    row = sheet.createRow(rowCount);
                    row.setHeightInPoints(40);
                    rowCount++;
                    cellHeader = row.createCell(0);
                    cellHeader.setCellType(CellType.STRING);
                    cellHeader.setCellValue("Код рейса");
                    cellHeader.setCellStyle(headStyle);

                    cellHeader = row.createCell(1);
                    cellHeader.setCellType(CellType.STRING);
                    cellHeader.setCellValue("Дата вылета");
                    cellHeader.setCellStyle(headStyle);

                    cellHeader = row.createCell(2);
                    cellHeader.setCellType(CellType.STRING);
                    cellHeader.setCellValue("Время вылета");
                    cellHeader.setCellStyle(headStyle);

                    cellHeader = row.createCell(3);
                    cellHeader.setCellType(CellType.STRING);
                    cellHeader.setCellValue("Аэропорт (код))");
                    cellHeader.setCellStyle(headStyle);

                    cellHeader = row.createCell(4);
                    cellHeader.setCellType(CellType.STRING);
                    cellHeader.setCellValue("Аэропорт назначения");
                    cellHeader.setCellStyle(headStyle);

                    cellHeader = row.createCell(5);
                    cellHeader.setCellType(CellType.STRING);
                    cellHeader.setCellValue("Город");
                    cellHeader.setCellStyle(headStyle);

                    cellHeader = row.createCell(6);
                    cellHeader.setCellType(CellType.STRING);
                    cellHeader.setCellValue("Страна");
                    cellHeader.setCellStyle(headStyle);

                    cellHeader = row.createCell(7);
                    cellHeader.setCellType(CellType.STRING);
                    cellHeader.setCellValue("Время полета, мин");
                    cellHeader.setCellStyle(headStyle);

                    cellHeader = row.createCell(8);
                    cellHeader.setCellType(CellType.STRING);
                    cellHeader.setCellValue("Статус рейса");
                    cellHeader.setCellStyle(headStyle);

                    cellHeader = row.createCell(9);
                    cellHeader.setCellType(CellType.STRING);
                    cellHeader.setCellValue("Компания");
                    cellHeader.setCellStyle(headStyle);

                    cellHeader = row.createCell(10);
                    cellHeader.setCellType(CellType.STRING);
                    cellHeader.setCellValue("Самолет");
                    cellHeader.setCellStyle(headStyle);



                    XSSFCellStyle dateStyle = workbook.createCellStyle();
                    dateStyle.setDataFormat((short) 14);
                    dateStyle.setBorderBottom(BorderStyle.THIN);
                    dateStyle.setBorderTop(BorderStyle.THIN);
                    dateStyle.setBorderLeft(BorderStyle.THIN);
                    dateStyle.setBorderRight(BorderStyle.THIN);


                    // заполним данными
                    for (Flight flight  : entry.getValue()) {
                        row = sheet.createRow(rowCount);
                        rowCount++;
                        XSSFCell cell = row.createCell(0);
                        cell.setCellStyle(cellStyle);
                        cell.setCellType(CellType.STRING);
                        cell.setCellValue(flight.getCode());

                        cell = row.createCell(1);
                        cell.setCellType(CellType.NUMERIC);
                        cell.setCellValue(flight.getDateDep());
                        cell.setCellStyle(dateStyle);

                        cell = row.createCell(2);
                        cell.setCellType(CellType.STRING);
                        cell.setCellValue(flight.getTimeDep().toString());
                        cell.setCellStyle(cellStyle);

                        cell = row.createCell(3);
                        cell.setCellType(CellType.STRING);
                        cell.setCellValue(flight.getAreaDestination().getCode());
                        cell.setCellStyle(cellStyle);

                        cell = row.createCell(4);
                        cell.setCellType(CellType.STRING);
                        cell.setCellValue(flight.getAreaDestination().getName());
                        cell.setCellStyle(cellStyle);

                        cell = row.createCell(5);
                        cell.setCellType(CellType.STRING);
                        cell.setCellValue(flight.getAreaDestination().getCity());
                        cell.setCellStyle(cellStyle);

                        cell = row.createCell(6);
                        cell.setCellType(CellType.STRING);
                        cell.setCellValue(flight.getAreaDestination().getCountry().getName());
                        cell.setCellStyle(cellStyle);

                        cell = row.createCell(7);
                        cell.setCellType(CellType.NUMERIC);
                        cell.setCellValue(flight.getFlightTime());
                        cell.setCellStyle(cellStyle);

                        cell = row.createCell(8);
                        cell.setCellType(CellType.STRING);
                        cell.setCellValue(flight.getStatus().getName());
                        cell.setCellStyle(cellStyle);

                        cell = row.createCell(9);
                        cell.setCellType(CellType.STRING);
                        cell.setCellValue(flight.getCompany().getName());
                        cell.setCellStyle(cellStyle);

                        cell = row.createCell(10);
                        cell.setCellType(CellType.STRING);
                        cell.setCellValue(flight.getAirplane().getName());
                        cell.setCellStyle(cellStyle);


                    }

                    // установим ширину для кажлой колонки
                    for (int j = 0; j < 11; j++) {    sheet.autoSizeColumn(j);                 }

                    i++;
                    this.updateProgress(i, map.keySet().size());

                }

                try {
                    // запишем в файл
                    FileOutputStream outputStream;
                    outputStream = new FileOutputStream(userHome + fileName);
                    workbook.write(outputStream);
                    workbook.close();
                    outputStream.close();
                    workbook = null;

                    updateMessage("Записано в " + fileName);
                } catch (FileNotFoundException ex) {
                    System.out.println(ex);
                    updateMessage(ex.getMessage());
                } catch (IOException ex) {
                    System.out.println(ex);
                    updateMessage(ex.getMessage());
                }

                return fileName;
            }
        };
    }
}

