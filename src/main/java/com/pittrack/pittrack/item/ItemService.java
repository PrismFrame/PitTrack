package com.pittrack.pittrack.item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pittrack.pittrack.account.TeamAccount;
import com.pittrack.pittrack.account.TeamAccountRepository;
import com.pittrack.pittrack.subteam.Subteam;
import com.pittrack.pittrack.subteam.SubteamRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.pittrack.pittrack.item.dto.ItemBackupDTO;
import com.pittrack.pittrack.item.dto.InventoryBackupDTO;

@Service
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository itemRepository;
    private final TeamAccountRepository teamAccountRepository;
    private final SubteamRepository subteamRepository;

    public ItemService(ItemRepository itemRepository, TeamAccountRepository teamAccountRepository, SubteamRepository subteamRepository) {
        this.itemRepository = itemRepository;
        this.teamAccountRepository = teamAccountRepository;
        this.subteamRepository = subteamRepository;
    }

    /** Get the team account for a given team number.
     * 
     * @param teamNumber The team number.
     * @return The team account.
     */
    private TeamAccount getTeam(String teamNumber) {
        return teamAccountRepository.findByTeamNumber(teamNumber)
            .orElseThrow(() -> new IllegalArgumentException(
                "Team account not found for team number: " + teamNumber
            ));
    }

    /** Get all items for a given team.
     * 
     * @param teamNumber The team number.
     * @return A list of all items for the specified team.
     */
    public List<Item> getAllItems(String teamNumber) {
        return itemRepository
            .findAllByTeamAccount_TeamNumber(teamNumber);
    }

    /** Get an item by ID for a given team.
     * 
     * @param id The ID of the item to retrieve.
     * @param teamNumber The team number.
     * @return The item with the specified ID for the specified team, or null if not found.
     */
    public Item getItemById(Long id, String teamNumber) {
        return itemRepository
            .findByIdAndTeamAccount_TeamNumber(id, teamNumber)
            .orElse(null);
    }

    /** 
     * Validates and retrieves subteams belonging to this team.
     */
    private Set<Subteam> getValidatedSubteams(List<Long> subteamIds, String teamNumber) {
        if (subteamIds == null || subteamIds.isEmpty()) {
            return new HashSet<>();
        }

        if (subteamIds.stream().anyMatch(id -> id == null)) {
            throw new IllegalArgumentException(
                "Invalid subteam selection."
            );
        }

        List<Long> uniqueIds = subteamIds.stream()
            .distinct()
            .toList();

        List<Subteam> subteams =
            subteamRepository
                .findAllByIdInAndTeamAccount_TeamNumber(
                    uniqueIds,
                    teamNumber
                );

        if (subteams.size() != uniqueIds.size()) {
            throw new IllegalArgumentException(
                "One or more selected subteams are invalid."
            );
        }

        return new HashSet<>(subteams);
    }

    /**
     * Assigns equipment access settings.
     */
    private void applySubteamSettings(Item item, List<Long> subteamIds, boolean sharedWithAll, String teamNumber) {
        if (sharedWithAll) {
            item.setSharedWithAll(true);
            item.getSubteams().clear();
            return;
        }

        if (subteamIds == null || subteamIds.isEmpty()) {
            throw new IllegalArgumentException(
                "Select at least one subteam."
            );
        }

        Set<Subteam> subteams =
            getValidatedSubteams(subteamIds, teamNumber);

        item.setSharedWithAll(false);
        item.setSubteams(subteams);
    }

    /** Add a new item to the inventory.
     * 
     * @param item The item to add.
     * @param teamNumber The team number.
     * @return The added item.
     */
    @Transactional
    public Item addItem(Item item, String teamNumber) {
        return addItem(item, teamNumber, null, true);
    }

    /**
     * Adds an item with subteam assignments.
     */
    @Transactional
    public Item addItem(Item item, String teamNumber, List<Long> subteamIds, boolean sharedWithAll) {
        TeamAccount team = getTeam(teamNumber);

        if (itemRepository.existsBySkuAndTeamAccount_TeamNumber(
                item.getSku(), teamNumber)) {

            throw new IllegalArgumentException(
                "SKU already exists: " + item.getSku()
            );
        }

        item.setTeamAccount(team);

        applySubteamSettings(
            item,
            subteamIds,
            sharedWithAll,
            teamNumber
        );

        return itemRepository.save(item);
    }

    /** Update an existing item, or return null if not found.
     * 
     * @param id The ID of the item to update.
     * @param updatedItem The updated item details.
     * @param teamNumber The team number.
     * @return The updated item, or null if not found.
     */
    @Transactional
    public Item updateItem(Long id, Item updatedItem, String teamNumber) {
        return updateItem(
            id,
            updatedItem,
            teamNumber,
            null,
            true
        );
    }

    /**
     * Updates an item and its subteam assignments.
     */
    @Transactional
    public Item updateItem(Long id, Item updatedItem, String teamNumber, List<Long> subteamIds, boolean sharedWithAll) {
        Item item = itemRepository
            .findByIdAndTeamAccount_TeamNumber(id, teamNumber)
            .orElse(null);

        if (item == null) {
            return null;
        }

        if (itemRepository
                .existsBySkuAndTeamAccount_TeamNumberAndIdNot(
                    updatedItem.getSku(),
                    teamNumber,
                    id
                )) {

            throw new IllegalArgumentException(
                "SKU already exists: " + updatedItem.getSku()
            );
        }

        item.setSku(updatedItem.getSku());
        item.setName(updatedItem.getName());
        item.setQuantity(updatedItem.getQuantity());
        item.setLocation(updatedItem.getLocation());
        item.setDescription(updatedItem.getDescription());
        item.setPrice(updatedItem.getPrice());
        item.setLowStockThreshold(updatedItem.getLowStockThreshold());

        applySubteamSettings(
            item,
            subteamIds,
            sharedWithAll,
            teamNumber
        );

        return itemRepository.save(item);
    }

    /** Delete an item by ID.
     * 
     * @param id The ID of the item to delete.
     * @param teamNumber The team number.
     * @return True if the item was deleted, false otherwise.
     */
    @Transactional
    public boolean deleteItem(Long id, String teamNumber) {
        Item item = itemRepository
            .findByIdAndTeamAccount_TeamNumber(id, teamNumber)
            .orElse(null);

        if (item == null) {
            return false;
        }

        itemRepository.delete(item);

        return true;
    }


    /**
     * Exports the inventory for one team.
     * 
     * @param teamNumber The team number.
     * @return An InventoryBackupDTO containing the exported inventory.
     */
    @Transactional(readOnly = true)
    public InventoryBackupDTO exportInventory(String teamNumber) {
        List<Item> items = itemRepository.findAllByTeamAccount_TeamNumber(teamNumber);

        List<ItemBackupDTO> backupItems = items.stream()
            .map(item -> new ItemBackupDTO(
                item.getSku(),
                item.getName(),
                item.getQuantity(),
                item.getLocation(),
                item.getDescription(),
                item.getPrice(),
                item.getLowStockThreshold(),
                item.isSharedWithAll(),
                item.getSubteams().stream()
                    .map(Subteam::getName)
                    .sorted()
                    .toList())).toList();

        return new InventoryBackupDTO("pittrack-backup", 1, java.time.LocalDateTime.now(), backupItems);
    }

    /**
     * Validates the backup before changing inventory.
     * 
     * @param backup The inventory backup to validate.
     * @throws IllegalArgumentException If the backup is invalid or unsupported.
     */
    private void validateBackup(InventoryBackupDTO backup) {
        if (backup == null || !"pittrack-backup".equals(backup.format()) || backup.version() != 1 || backup.items() == null) {
            throw new IllegalArgumentException("Invalid or unsupported PitTrack backup file.");
        }

        if (backup.items().size() > 10000) {
            throw new IllegalArgumentException("Backup contains too many items.");
        }

        Set<String> seenSkus = new HashSet<>();

        for (ItemBackupDTO dto : backup.items()) {

            if (dto == null) {
                throw new IllegalArgumentException("Backup contains an invalid item.");
            }

            if (dto.name() == null || dto.name().isBlank() || dto.name().length() > 100) {
                throw new IllegalArgumentException("Every item must have a valid name.");
            }

            if (dto.location() == null || dto.location().isBlank() || dto.location().length() > 100) {
                throw new IllegalArgumentException("Every item must have a valid location.");
            }

            if (dto.description() == null || dto.description().isBlank() || dto.description().length() > 500) {
                throw new IllegalArgumentException( "Every item must have a valid description.");
            }

            if (dto.quantity() < 0) {
                throw new IllegalArgumentException("Item quantity cannot be negative.");
            }

            if (dto.price() == null || dto.price().compareTo(BigDecimal.ZERO) < 0 || dto.price().precision() > 10 || dto.price().scale() > 2) {
                throw new IllegalArgumentException("Every item must have a valid price.");
            }

            if (dto.lowStockThreshold() == null || dto.lowStockThreshold() < 0) {
                throw new IllegalArgumentException("Low stock threshold cannot be negative or null.");
            }

            if (dto.sku() != null && dto.sku().trim().length() > 50) {
                throw new IllegalArgumentException("SKU cannot exceed 50 characters.");
            }

            String sku = dto.sku() == null ? null : dto.sku().trim();

            if (sku != null && !sku.isEmpty() && !seenSkus.add(sku)) {
                throw new IllegalArgumentException("Backup contains duplicate SKU: " + sku);
            }

            if (!dto.sharedWithAll() && (dto.subteamNames() == null || dto.subteamNames().isEmpty())) {
                throw new IllegalArgumentException("Restricted items must have at least one subteam.");
            }
        }
    }

    /**
     * Resolves subteam names against the destination team.
     * 
     * @param teamNumber The team number.
     * @return A map of lowercased subteam names to Subteam objects for the team.
     * @throws IllegalArgumentException If a subteam name in the backup does not exist in the destination team.
     */
    private Map<String, Subteam> getTeamSubteamMap(String teamNumber) {
        Map<String, Subteam> result = new HashMap<>();

        for (Subteam subteam : subteamRepository .findAllByTeamAccount_TeamNumberOrderByNameAsc(teamNumber)) {
            result.put(subteam.getName().trim().toLowerCase(Locale.ROOT), subteam);
        }

        return result;
    }

    /**
     * Creates a new inventory item from a backup DTO.
     * 
     * @param dto The item backup DTO.
     * @param team The team account.
     * @param subteamMap A map of subteam names to Subteam objects for the team.
     * @return The created Item entity.
     */
    private Item createImportedItem(ItemBackupDTO dto, TeamAccount team, Map<String, Subteam> subteamMap) {
        Item item = new Item();

        item.setTeamAccount(team);
        item.setSku(dto.sku() == null || dto.sku().isBlank() ? null : dto.sku().trim());
        item.setName(dto.name().trim());
        item.setQuantity(dto.quantity());
        item.setLowStockThreshold(dto.lowStockThreshold());
        item.setLocation(dto.location().trim());
        item.setDescription(dto.description().trim());
        item.setPrice(dto.price());

        if (dto.sharedWithAll()) {
            item.setSharedWithAll(true);
            item.setSubteams(new HashSet<>());
        } else {
            Set<Subteam> selectedSubteams = new HashSet<>();

            for (String name : dto.subteamNames()) {

                if (name == null || name.isBlank()) {
                    throw new IllegalArgumentException("Backup contains an invalid subteam name.");
                }

                Subteam subteam = subteamMap.get(name.trim().toLowerCase(Locale.ROOT));

                if (subteam == null) {
                    throw new IllegalArgumentException("Subteam not found in your team: " + name);
                }

                selectedSubteams.add(subteam);
            }

            if (selectedSubteams.isEmpty()) {
                throw new IllegalArgumentException("Restricted items must have at least one valid subteam.");
            }

            item.setSharedWithAll(false);
            item.setSubteams(selectedSubteams);
        }

        return item;
    }

    /**
     * Merges imported items without overwriting existing SKUs.
     * 
     * @param backup The inventory backup to merge.
     * @param teamNumber The team number.
     * @return The number of items added to the inventory.
     */
    @Transactional
    public int mergeInventory(InventoryBackupDTO backup, String teamNumber) {
        validateBackup(backup);

        TeamAccount team = getTeam(teamNumber);
        Map<String, Subteam> subteamMap = getTeamSubteamMap(teamNumber);
        int imported = 0;

        for (ItemBackupDTO dto : backup.items()) {
            String sku = dto.sku() == null ? null : dto.sku().trim();

            if (sku != null && !sku.isEmpty() && itemRepository.existsBySkuAndTeamAccount_TeamNumber(sku, teamNumber)) {
                continue;
            }

            Item item = createImportedItem(dto, team, subteamMap);

            itemRepository.save(item);
            imported++;
        }

        return imported;
    }

    /**
     * Replaces all inventory belonging to the specified team.
     * 
     * @param backup The inventory backup to restore.
     * @param teamNumber The team number.
     * @return The number of items imported.
     * @throws IllegalArgumentException If the backup is invalid or unsupported.
     */
    @Transactional
    public int replaceInventory(InventoryBackupDTO backup, String teamNumber) {
        validateBackup(backup);

        TeamAccount team = getTeam(teamNumber);
        Map<String, Subteam> subteamMap = getTeamSubteamMap(teamNumber);

        // Build every item before deleting anything.
        List<Item> importedItems = new ArrayList<>();

        for (ItemBackupDTO dto : backup.items()) {
            importedItems.add(createImportedItem(dto, team, subteamMap));
        }

        // Delete only this team's existing inventory.
        List<Item> existingItems = itemRepository.findAllByTeamAccount_TeamNumber(teamNumber);

        itemRepository.deleteAll(existingItems);
        itemRepository.saveAll(importedItems);

        return importedItems.size();
    }
}
