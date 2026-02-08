package com.example.core_service.imports;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ContactImportService {

    public List<ImportedContact> parseContacts(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            return parseCsv(file);
        }
        return parseExcel(file);
    }

    private List<ImportedContact> parseCsv(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .build()
                     .parse(reader)) {

            List<ImportedContact> contacts = new ArrayList<>();
            Map<String, String> headerMap = normalizeHeaders(parser.getHeaderMap().keySet());

            for (CSVRecord record : parser) {
                String email = getValue(record, headerMap, "email");
                if (email == null || email.isBlank()) {
                    continue;
                }
                contacts.add(new ImportedContact(
                        email.trim(),
                        getValue(record, headerMap, "first_name"),
                        getValue(record, headerMap, "last_name")
                ));
            }
            return contacts;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to parse CSV file", ex);
        }
    }

    private List<ImportedContact> parseExcel(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return List.of();
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                return List.of();
            }

            Map<String, Integer> headerIndexes = new HashMap<>();
            for (Cell cell : headerRow) {
                String header = cell.getStringCellValue();
                if (header != null) {
                    headerIndexes.put(normalizeHeader(header), cell.getColumnIndex());
                }
            }

            List<ImportedContact> contacts = new ArrayList<>();
            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                String email = getCellValue(row, headerIndexes.get("email"));
                if (email == null || email.isBlank()) {
                    continue;
                }
                contacts.add(new ImportedContact(
                        email.trim(),
                        getCellValue(row, headerIndexes.get("first_name")),
                        getCellValue(row, headerIndexes.get("last_name"))
                ));
            }
            return contacts;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to parse Excel file", ex);
        }
    }

    private String getCellValue(Row row, Integer index) {
        if (index == null) {
            return null;
        }
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private Map<String, String> normalizeHeaders(Iterable<String> headers) {
        Map<String, String> normalized = new HashMap<>();
        for (String header : headers) {
            if (header == null) {
                continue;
            }
            normalized.put(normalizeHeader(header), header);
        }
        return normalized;
    }

    private String normalizeHeader(String header) {
        return header.trim().toLowerCase(Locale.ROOT)
                .replace("first name", "first_name")
                .replace("last name", "last_name")
                .replace("firstname", "first_name")
                .replace("lastname", "last_name")
                .replace("first_name", "first_name")
                .replace("last_name", "last_name");
    }

    private String getValue(CSVRecord record, Map<String, String> headerMap, String headerKey) {
        String header = headerMap.get(headerKey);
        if (header == null) {
            return null;
        }
        return record.get(header);
    }
}
