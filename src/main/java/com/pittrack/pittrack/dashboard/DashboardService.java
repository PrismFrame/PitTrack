
package com.pittrack.pittrack.dashboard;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pittrack.pittrack.item.Item;
import com.pittrack.pittrack.item.ItemRepository;
import com.pittrack.pittrack.subteam.SubteamRepository;

@Service
public class DashboardService {
    private static final int LOW_STOCK_THRESHOLD = 5;
    private final ItemRepository itemRepository;
    private final SubteamRepository subteamRepository;

    public DashboardService(ItemRepository itemRepository, SubteamRepository subteamRepository) {
        this.itemRepository = itemRepository;
        this.subteamRepository = subteamRepository;
    }

    public DashboardStats getStats(String teamNumber, Long subteamId) {
        List<Item> items;

        if (subteamId == null) {
            items = itemRepository
                .findAllByTeamAccount_TeamNumber(teamNumber);
        } else {

            if (!subteamRepository
                    .existsByIdAndTeamAccount_TeamNumber(
                        subteamId,
                        teamNumber)) {

                throw new IllegalArgumentException(
                    "Invalid subteam selection."
                );
            }

            items = itemRepository.findVisibleToSubteam(
                teamNumber,
                subteamId
            );
        }

        long totalItems = items.size();

        long totalUnits = items.stream()
            .mapToLong(Item::getQuantity)
            .sum();

        long lowStock = items.stream()
            .filter(i -> i.getQuantity() > 0
                && i.getQuantity() <= LOW_STOCK_THRESHOLD)
            .count();

        long inStock = items.stream()
            .filter(i -> i.getQuantity() > LOW_STOCK_THRESHOLD)
            .count();

        long outOfStock = items.stream()
            .filter(i -> i.getQuantity() == 0)
            .count();

        BigDecimal inventoryValue = items.stream()
            .map(i -> i.getPrice().multiply(
                BigDecimal.valueOf(i.getQuantity())
            ))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Item> recentItems = items.stream()
            .sorted(
                Comparator.comparing(
                    Item::getId,
                    Comparator.nullsLast(
                        Comparator.reverseOrder()
                    )
                )
            )
            .limit(5)
            .toList();

        return new DashboardStats(
            totalItems,
            totalUnits,
            lowStock,
            inStock,
            outOfStock,
            inventoryValue,
            recentItems
        );
    }
}
