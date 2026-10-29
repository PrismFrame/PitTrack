
package com.pittrack.pittrack.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pittrack.pittrack.model.Item;
import com.pittrack.pittrack.repository.ItemRepository;

@Service
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /** Get all items in the inventory.
     * 
     * @return A list of all items in the inventory.
     */
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    /** Get an item by ID, or null if it does not exist.
     * 
     * @param id The ID of the item to retrieve.
     * @return The item with the specified ID, or null if not found.
     */
    public Item getItemById(Long id) {
        return itemRepository.findById(id).orElse(null);
    }

    /** Add a new item to the inventory.
     * 
     * @param item The item to add.
     * @return The added item.
     */
    @Transactional
    public Item addItem(Item item) {
        if (itemRepository.existsBySku(item.getSku())) {
        throw new IllegalArgumentException(
                "SKU already exists: " + item.getSku());
        }

    return itemRepository.save(item);
    }

    /** Update an existing item, or return null if not found.
     * 
     * @param id The ID of the item to update.
     * @param updatedItem The updated item details.
     * @return The updated item, or null if not found.
     */
    @Transactional
    public Item updateItem(Long id, Item updatedItem) {
        return itemRepository.findById(id)
                .map(item -> {
                    if (itemRepository.existsBySkuAndIdNot(
                        updatedItem.getSku(), id)) {
                    throw new IllegalArgumentException(
                            "SKU already exists: "
                            + updatedItem.getSku());
                    }

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

    /** Delete an item by ID.
     * 
     * @param id The ID of the item to delete.
     */
    @Transactional
    public void deleteItem(Long id) {
        if (itemRepository.existsById(id)) {
            itemRepository.deleteById(id);
        }
    }
}
