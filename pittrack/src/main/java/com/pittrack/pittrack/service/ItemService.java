package com.pittrack.pittrack.service;

import java.util.List;

import org.springframework.stereotype.Service;
import com.pittrack.pittrack.model.Item;
import com.pittrack.pittrack.repository.ItemRepository;

@Service 
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /** Get all the items in the inventory.
     * 
     * @return A list of all items in the inventory.
     */
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    /** Get item by ID.
     * 
     * @param id The ID of the item to be retrieved.
     * @return The item with the specified ID, or null if not found.
     */
    public Item getItemById(Long id) {
        return itemRepository.findById(id).orElse(null);
    }

    /** Add new item to inventory.
     * 
     * @param item The item to be added.
     * @return The added item.
     */
    public Item addItem(Item item) {
        return itemRepository.save(item);
    }

    /** Update existing item in inventory.
     * 
     * @param id The ID of the item to be updated.
     * @param updatedItem The updated item details.
     * @return The updated item, or null if the item with the specified ID is not found.
     */
    public Item updateItem(Long id, Item updatedItem) {
        return itemRepository.findById(id)
                .map(item -> {
                    item.setSku(updatedItem.getSku());
                    item.setName(updatedItem.getName());
                    item.setQuantity(updatedItem.getQuantity());
                    item.setLocation(updatedItem.getLocation());
                    item.setDescription(updatedItem.getDescription());
                    item.setPrice(updatedItem.getPrice());
                    return itemRepository.save(item);
                })
                .orElse(null);
    }

    /** Delete item from inventory using ID.
     * 
     * @param id The ID of the item to be deleted.
     */
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }
}
