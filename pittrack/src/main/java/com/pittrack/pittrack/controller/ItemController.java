package com.pittrack.pittrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.pittrack.pittrack.model.Item;
import com.pittrack.pittrack.service.ItemService;


@Controller 
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /** Displays all inventory items.
     * 
     * @param model The model to which the list of items will be added.
     * @return The name of the view that will display the items.
     */
    @GetMapping("/items")
    public String displayAllItems(Model model) {
        model.addAttribute("items", itemService.getAllItems());
        return "items";
    }

    /** Adds a new inventory item.
     * 
     * @param item The item to add.
     * @return The name of the view to which the user will be redirected.
     */
    @PostMapping("/items")
    public String addItem(@ModelAttribute Item item) {
        itemService.addItem(item);
        return "redirect:/items";
    }

    /** Updates an existing inventory item.
     * 
     * @param id The ID of the item to update.
     * @param updatedItem The updated item details.
     * @return The name of the view to which the user will be redirected.
     */
    @PostMapping("/items/{id}")
    public String updateItem(@PathVariable Long id, @ModelAttribute Item updatedItem) {
        itemService.updateItem(id, updatedItem);
        return "redirect:/items";
    }

    /** Deletes an existing inventory item.
     * 
     * @param id The ID of the item to delete.
     * @return The name of the view to which the user will be redirected.
     */
    @PostMapping("/items/{id}/delete")
    public String deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return "redirect:/items";
    }

    /** Displays the form for updating an existing inventory item.
     * 
     * @param id The ID of the item to update.
     * @param model The model to which the item details will be added.
     * @return The name of the view that will display the update form.
     */
    @GetMapping("/items/{id}/update")
    public String updateItem(@PathVariable Long id, Model model) {
        Item item = itemService.getItemById(id);
        if (item != null) {
            model.addAttribute("item", item);
            return "updateItem";
        } else {
            return "redirect:/items"; // Redirect to items list if item not found
        }
    }

    
    /** Displays details for a specific inventory item.
     * 
     * @param id The ID of the item to display.
     * @param model The model to which the item details will be added.
     * @return The name of the view that will display the item details.
     */
    @GetMapping("/items/{id}")
    public String displayItemDetails(@PathVariable Long id, Model model) {
        model.addAttribute("item", itemService.getItemById(id));
        return "itemDetails";
    }

}
