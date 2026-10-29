
package com.pittrack.pittrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.pittrack.pittrack.model.Item;
import com.pittrack.pittrack.service.ItemService;

import jakarta.validation.Valid;

@Controller
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /** Displays all inventory items.
     * 
     * @param model The model to hold attributes for the view.
     * @return The view name for displaying all items.
     */
    @GetMapping("/items")
    public String displayAllItems(Model model) {
        model.addAttribute("items", itemService.getAllItems());
        model.addAttribute("item", new Item());
        return "items";
    }

    /** Adds a new inventory item.
     * 
     * @param item The item to add.
     * @param result The binding result for validation.
     * @param model The model to hold attributes for the view.
     * @return The view name for displaying all items.
     */
    @PostMapping("/items")
    public String addItem(
            @Valid @ModelAttribute("item") Item item,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("items", itemService.getAllItems());
            return "items";
        }

        try {
            itemService.addItem(item);
        } catch (IllegalArgumentException ex) {
            result.rejectValue("sku", "duplicate", ex.getMessage());
            model.addAttribute("items", itemService.getAllItems());
            return "items";
        }

        return "redirect:/items";
    }

    /** Displays the form for updating an existing item.
     * 
     * @param id The ID of the item to update.
     * @param model The model to hold attributes for the view.
     * @return The view name for the update form.
     */
    @GetMapping("/items/{id}/update")
    public String showUpdateForm(
            @PathVariable Long id,
            Model model) {

        Item item = itemService.getItemById(id);

        if (item == null) {
            return "redirect:/items";
        }

        model.addAttribute("item", item);
        model.addAttribute("itemId", id);

        return "updateItem";
    }

    /** Updates an existing inventory item.
     * 
     * @param id The ID of the item to update.
     * @param updatedItem The updated item details.
     * @param result The binding result for validation.
     * @param model The model to hold attributes for the view.
     * @return The view name for displaying all items.
     */
    @PostMapping("/items/{id}/update")
    public String updateItem(
            @PathVariable Long id,
            @Valid @ModelAttribute("item") Item updatedItem,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("itemId", id);
            return "updateItem";
        }

        Item existingItem = itemService.getItemById(id);

        if (existingItem == null) {
            return "redirect:/items";
        }

        try {
            itemService.updateItem(id, updatedItem);
        } catch (IllegalArgumentException ex) {
            result.rejectValue("sku", "duplicate", ex.getMessage());
            model.addAttribute("itemId", id);
            return "updateItem";
        }

        return "redirect:/items";
    }

    /** Deletes an existing inventory item.
     * 
     * @param id The ID of the item to delete.
     * @return The view name for displaying all items.
     */
    @PostMapping("/items/{id}/delete")
    public String deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return "redirect:/items";
    }

    /** Displays details for a specific inventory item.
     * 
     * @param id The ID of the item to display.
     * @param model The model to hold attributes for the view.
     * @return The view name for displaying item details.
     */
    @GetMapping("/items/{id}")
    public String displayItemDetails(
            @PathVariable Long id,
            Model model) {

        Item item = itemService.getItemById(id);

        if (item == null) {
            return "redirect:/items";
        }

        model.addAttribute("item", item);
        return "itemDetails";
    }
}
