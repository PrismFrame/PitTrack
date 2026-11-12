package com.pittrack.pittrack.item;

import java.io.IOException;
import java.security.Principal;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pittrack.pittrack.item.dto.InventoryBackupDTO;

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
     * Import a JSON inventory backup.
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

            InventoryBackupDTO backup = objectMapper.readValue(file.getInputStream(), InventoryBackupDTO.class);
            int count;

            if ("merge".equals(mode)) {
                count = itemService.mergeInventory(backup, principal.getName());
                redirectAttributes.addFlashAttribute("successMessage", count + " items imported successfully. " + "Existing SKUs were skipped.");
            } else if ("replace".equals(mode)) {
                count = itemService.replaceInventory(backup, principal.getName());
                redirectAttributes.addFlashAttribute("successMessage", "Inventory replaced successfully. " + count + " items restored.");
            } else {
                throw new IllegalArgumentException("Invalid import mode.");
            }
            
        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to read backup file. " + "Make sure it is a valid JSON file.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",ex.getMessage());
        }

        return "redirect:/items";
    }
}
