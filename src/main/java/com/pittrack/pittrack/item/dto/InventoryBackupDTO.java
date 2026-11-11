package com.pittrack.pittrack.item.dto;

import java.time.LocalDateTime;
import java.util.List;

public record InventoryBackupDTO(String format, int version, LocalDateTime exportedAt, List<ItemBackupDTO> items) {

}
