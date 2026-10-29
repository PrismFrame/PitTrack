
package com.pittrack.pittrack.item;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pittrack.pittrack.account.TeamAccount;
import com.pittrack.pittrack.account.TeamAccountRepository;

@Service
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository itemRepository;
    private final TeamAccountRepository teamAccountRepository;

    public ItemService(ItemRepository itemRepository, TeamAccountRepository teamAccountRepository) {
        this.itemRepository = itemRepository;
        this.teamAccountRepository = teamAccountRepository;
    }

    /** Get the team account for a given team number.
     * 
     * @param teamNumber The team number.
     * @return The team account.
     */
    private TeamAccount getTeam(String teamNumber) {
        return teamAccountRepository.findByTeamNumber(teamNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Team account not found for team number: " + teamNumber));
    }

    /** Get all items for a given team.
     * 
     * @param teamNumber The team number.
     * @return A list of all items for the specified team.
     */
    public List<Item> getAllItems(String teamNumber) {
        return itemRepository.findAllByTeamAccount_TeamNumber(teamNumber);
    }

    /** Get an item by ID for a given team.
     * 
     * @param id The ID of the item to retrieve.
     * @param teamNumber The team number.
     * @return The item with the specified ID for the specified team, or null if not found.
     */
    public Item getItemById(Long id, String teamNumber) {
        return itemRepository.findByIdAndTeamAccount_TeamNumber(id, teamNumber)
                .orElse(null);
    }

    /** Add a new item to the inventory.
     * 
     * @param item The item to add.
     * @param teamNumber The team number.
     * @return The added item.
     */
    @Transactional
    public Item addItem(Item item, String teamNumber) {
        TeamAccount team = getTeam(teamNumber);

        if (itemRepository.existsBySkuAndTeamAccount_TeamNumber(item.getSku(), teamNumber)) {
        throw new IllegalArgumentException(
                "SKU already exists: " + item.getSku());
        }

        item.setTeamAccount(team);
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
        Item item = itemRepository
                    .findByIdAndTeamAccount_TeamNumber(id, teamNumber).orElse(null);
        
        if (item == null) {
            return null;
        }

        if (itemRepository.existsBySkuAndTeamAccount_TeamNumberAndIdNot(
                updatedItem.getSku(), teamNumber, id)) {
            throw new IllegalArgumentException(
                "SKU already exists: " + updatedItem.getSku());
        }

        item.setSku(updatedItem.getSku());
        item.setName(updatedItem.getName());
        item.setQuantity(updatedItem.getQuantity());
        item.setLocation(updatedItem.getLocation());
        item.setDescription(updatedItem.getDescription());
        item.setPrice(updatedItem.getPrice());

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
                    .findByIdAndTeamAccount_TeamNumber(id, teamNumber).orElse(null);

        if (item == null) {
            return false;
        }

        itemRepository.delete(item);
        return true;
    }
}
