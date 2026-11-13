package com.pittrack.pittrack.item;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pittrack.pittrack.item.dto.InventoryBackupDTO;
import com.pittrack.pittrack.item.dto.ItemBackupDTO;

@Controller
@RequestMapping("/items")
public class ItemImportExportController {
    private final ItemService itemService;
    private final ObjectMapper objectMapper;

    public ItemImportExportController(ItemService itemService, ObjectMapper objectMapper) {
        this.itemService = itemService;
        this.objectMapper = objectMapper;
    }

    /**
     * Download a JSON inventory backup.
     * 
     * @param principal The authenticated user.
     * @return A ResponseEntity containing the JSON file.
     * @throws IOException If an I/O error occurs while generating the JSON.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportInventory(Principal principal) throws IOException {
        InventoryBackupDTO backup = itemService.exportInventory(principal.getName());

        byte[] json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(backup);

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename("pittrack-backup.json")
                    .build().toString()).body(json);
    }
    
    /**
     * Import a JSON or Excel inventory backup.
     * 
     * @param file The uploaded backup file.
     * @param mode The import mode ("merge" or "replace").
     * @param principal The authenticated user.
     * @param redirectAttributes Attributes for flash messages.
     * @return A redirect to the inventory page.
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String importInventory(@RequestParam("file") MultipartFile file, @RequestParam("mode") String mode, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Please select a backup file.");
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("Backup file cannot exceed 5 MB.");
            }

            String filename = file.getOriginalFilename();

            if (filename == null) {
                throw new IllegalArgumentException("Invalid backup filename.");
            }

            InventoryBackupDTO backup;

            if (filename.toLowerCase().endsWith(".json")) {
                backup = objectMapper.readValue(file.getInputStream(), InventoryBackupDTO.class);
            } else if (filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls")) {
                backup = readExcelBackup(file, principal.getName());
            } else {
                throw new IllegalArgumentException("Unsupported file type. Please upload a JSON or Excel backup.");
            }

            int count;

            if ("merge".equals(mode)) {
                count = itemService.mergeInventory(backup, principal.getName());
                redirectAttributes.addFlashAttribute("successMessage", count + " items imported successfully. Existing SKUs were skipped.");
            } else if ("replace".equals(mode)) {
                count = itemService.replaceInventory(backup, principal.getName());
                redirectAttributes.addFlashAttribute("successMessage", "Inventory replaced successfully. " + count + " items restored.");
            } else {
                throw new IllegalArgumentException("Invalid import mode.");
            }

        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to read backup file. Make sure it is a valid JSON or Excel file.");

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/items";
    }

    /**
     * Download an Excel inventory backup.
     * 
     * @param principal The authenticated user.
     * @return A ResponseEntity containing the Excel file.
     */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportInventoryExcel(Principal principal) throws IOException {
        InventoryBackupDTO backup = itemService.exportInventory(principal.getName());

        try (Workbook workbook = new XSSFWorkbook();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Inventory");

            // Header styling
            CellStyle headerStyle = workbook.createCellStyle();

            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Font headerFont = workbook.createFont();

            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 12);

            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Alternating row styles
            CellStyle normalStyle = workbook.createCellStyle();
            CellStyle alternateStyle = workbook.createCellStyle();

            alternateStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            alternateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Currency formatting
            CellStyle priceStyle = workbook.createCellStyle();

            priceStyle.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));

            CellStyle alternatePriceStyle = workbook.createCellStyle();

            alternatePriceStyle.cloneStyleFrom(alternateStyle);
            alternatePriceStyle.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));

            // Column headers
            String[] headers = {
                "SKU",
                "Name",
                "Quantity",
                "Location",
                "Description",
                "Price",
                "Low Stock Threshold",
                "Shared With All",
                "Subteams"
            };

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);

                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Inventory data
            int rowNum = 1;

            for (ItemBackupDTO item : backup.items()) {
                Row row = sheet.createRow(rowNum);
                boolean alternate = rowNum % 2 == 0;
                CellStyle rowStyle = alternate ? alternateStyle : normalStyle;
                CellStyle rowPriceStyle = alternate ? alternatePriceStyle : priceStyle;

                // SKU
                Cell skuCell = row.createCell(0);

                skuCell.setCellValue(item.sku() != null ? item.sku() : "");
                skuCell.setCellStyle(rowStyle);

                // Name
                Cell nameCell = row.createCell(1);

                nameCell.setCellValue(item.name() != null ? item.name() : "");
                nameCell.setCellStyle(rowStyle);

                // Quantity
                Cell quantityCell = row.createCell(2);

                quantityCell.setCellValue(item.quantity());
                quantityCell.setCellStyle(rowStyle);

                // Location
                Cell locationCell = row.createCell(3);

                locationCell.setCellValue(item.location() != null ? item.location() : "");
                locationCell.setCellStyle(rowStyle);

                // Description
                Cell descriptionCell = row.createCell(4);

                descriptionCell.setCellValue(item.description() != null ? item.description() : "");
                descriptionCell.setCellStyle(rowStyle);

                // Price
                Cell priceCell = row.createCell(5);

                if (item.price() != null) {
                    priceCell.setCellValue(item.price().doubleValue());
                } else {
                    priceCell.setCellValue(0);
                }

                priceCell.setCellStyle(rowPriceStyle);

                // Low stock threshold
                Cell thresholdCell = row.createCell(6);

                if (item.lowStockThreshold() != null) {
                    thresholdCell.setCellValue(item.lowStockThreshold());
                } else {
                    thresholdCell.setBlank();
                }

                thresholdCell.setCellStyle(rowStyle);

                // Shared with all
                Cell sharedCell = row.createCell(7);

                sharedCell.setCellValue(item.sharedWithAll() ? "Yes" : "No");
                sharedCell.setCellStyle(rowStyle);

                // Subteam names
                Cell subteamCell = row.createCell(8);
                String subteams = item.subteamNames() != null ? String.join(", ", item.subteamNames()) : "";

                subteamCell.setCellValue(subteams);
                subteamCell.setCellStyle(rowStyle);
                rowNum++;
            }

            // Enable filtering on all columns
            if (rowNum > 1) {
                sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, rowNum - 1, 0, headers.length - 1));
            }

            // Freeze header row
            sheet.createFreezePane(0, 1);

            // Automatically resize columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);

                // Prevent excessively wide columns
                if (sheet.getColumnWidth(i) > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }

            // Write workbook
            workbook.write(outputStream);
            byte[] excel = outputStream.toByteArray();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                            .filename("pittrack-inventory.xlsx")
                            .build()
                            .toString())
                    .body(excel);
        }
    }

    /**
     * Read an Excel file and convert it into an inventory backup DTO.
     * 
     * @param file The uploaded Excel file.
     * @param username The username of the authenticated user.
     * @return An InventoryBackupDTO containing the parsed items.
     * @throws IOException If an I/O error occurs while reading the file.
     */
    private InventoryBackupDTO readExcelBackup(MultipartFile file, String username) throws IOException {
        List<ItemBackupDTO> items = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheet("Inventory");

            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }

            if (sheet.getLastRowNum() < 1) {
                throw new IllegalArgumentException("The Excel file does not contain any inventory items.");
            }

            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            // Validate headers
            Row header = sheet.getRow(0);

            String[] expectedHeaders = {
                "SKU",
                "Name",
                "Quantity",
                "Location",
                "Description",
                "Price",
                "Low Stock Threshold",
                "Shared With All",
                "Subteams"
            };

            if (header == null) {
                throw new IllegalArgumentException("Invalid Excel backup: missing header row.");
            }

            for (int i = 0; i < expectedHeaders.length; i++) {
                Cell cell = header.getCell(i);
                String actual = cell == null ? "" : formatter.formatCellValue(cell, evaluator).trim();

                if (!expectedHeaders[i].equalsIgnoreCase(actual)) {
                    throw new IllegalArgumentException("Invalid Excel backup. Expected column " + (i + 1) + " to be " + expectedHeaders[i] + ".");
                }
            }

            // Read inventory rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                String sku = getCellValue(row, 0, formatter, evaluator);
                String name = getCellValue(row, 1, formatter, evaluator);

                // Skip completely empty rows
                if (sku.isBlank() && name.isBlank()) {
                    continue;
                }

                try {
                    int quantity = Integer.parseInt(getCellValue(row, 2, formatter, evaluator));
                    String location = getCellValue(row, 3, formatter, evaluator);
                    String description = getCellValue(row, 4, formatter, evaluator);
                    String priceValue = getCellValue(row, 5, formatter, evaluator)
                                    .replace("$", "")
                                    .replace(",", "")
                                    .trim();
                    BigDecimal price = priceValue.isBlank() ? BigDecimal.ZERO : new BigDecimal(priceValue);
                    String thresholdValue = getCellValue(row, 6, formatter, evaluator);
                    Integer threshold = thresholdValue.isBlank() ? null : Integer.valueOf(thresholdValue);
                    String sharedValue = getCellValue(row, 7, formatter, evaluator);
                    boolean sharedWithAll = sharedValue.equalsIgnoreCase("yes") || sharedValue.equalsIgnoreCase("true");
                    String subteamValue = getCellValue(row, 8, formatter, evaluator);
                    List<String> subteamNames = subteamValue.isBlank()
                            ? new ArrayList<>()
                            : java.util.Arrays.stream(
                                    subteamValue.split(","))
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .toList();
                    ItemBackupDTO item = new ItemBackupDTO(
                            sku,
                            name,
                            quantity,
                            location,
                            description,
                            price,
                            threshold,
                            sharedWithAll,
                            subteamNames
                    );

                    items.add(item);

                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Invalid number in Excel row " + (i + 1) + ". Check quantity, price, and low stock threshold.");
                }
            }
        }

        if (items.isEmpty()) {
            throw new IllegalArgumentException("No valid inventory items were found in the Excel file.");
        }

        // Reuse the application's existing backup metadata.
        InventoryBackupDTO metadata = itemService.exportInventory(username);

        return new InventoryBackupDTO(
                metadata.format(),
                metadata.version(),
                java.time.LocalDateTime.now(),
                items
        );
    }

    /**
     * Safely read a cell as text.
     * 
     * @param row The row containing the cell.
     * @param column The column index of the cell.
     * @param formatter The data formatter.
     * @param evaluator The formula evaluator.
     * @return The text value of the cell.
     */
    private String getCellValue(Row row, int column, DataFormatter formatter, FormulaEvaluator evaluator) {
        Cell cell = row.getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        if (cell == null) {
            return "";
        }

        return formatter.formatCellValue(cell, evaluator).trim();
    }
}
