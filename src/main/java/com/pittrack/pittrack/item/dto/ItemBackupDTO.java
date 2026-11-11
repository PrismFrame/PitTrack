package com.pittrack.pittrack.item.dto;

import java.math.BigDecimal;
import java.util.List;

public record ItemBackupDTO(String sku, String name, int quantity, String location, String description, BigDecimal price, boolean sharedWithAll, List<String> subteamNames) {

}
