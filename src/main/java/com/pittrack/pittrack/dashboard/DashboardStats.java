package com.pittrack.pittrack.dashboard;

import java.math.BigDecimal;
import java.util.List;

import com.pittrack.pittrack.item.Item;

public class DashboardStats {
    private final long totalItems;
    private final long totalUnits;
    private final long inStock;
    private final long lowStock;
    private final long outOfStock;
    private final BigDecimal inventoryValue;
    private final List<Item> recentItems;

    public DashboardStats(long totalItems, long totalUnits, long inStock, long lowStock, long outOfStock, BigDecimal inventoryValue, List<Item> recentItems) {
        this.totalItems = totalItems;
        this.totalUnits = totalUnits;
        this.inStock = inStock;
        this.lowStock = lowStock;
        this.outOfStock = outOfStock;
        this.inventoryValue = inventoryValue;
        this.recentItems = recentItems;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public long getTotalUnits() {
        return totalUnits;
    }

    public long getInStock() {
        return inStock;
    }

    public long getLowStock() {
        return lowStock;
    }

    public long getOutOfStock() {
        return outOfStock;
    }

    public BigDecimal getInventoryValue() {
        return inventoryValue;
    }

    public List<Item> getRecentItems() {
        return recentItems;
    }
}
