
package com.pittrack.pittrack.item;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.pittrack.pittrack.subteam.SubteamService;

import jakarta.validation.Valid;

@Controller
public class ItemController {
    private final ItemService itemService;
    private final SubteamService subteamService;

    public ItemController(ItemService itemService, SubteamService subteamService) {
        this.itemService = itemService;
        this.subteamService = subteamService;
    }

    public void populateForm(Model model, String teamNumber) {
        model.addAttribute("subteams", subteamService.getSubteams(teamNumber));
    }

    /** Displays all inventory items.
     * 
     * @param model The model to hold attributes for the view.
     * @param principal The principal representing the currently authenticated user.
     * @return The view name for displaying all items.
     */
    @GetMapping("/items")
    public String displayAllItems(Model model, Principal principal) {
        String teamNumber = principal.getName();

        model.addAttribute("teamNumber", teamNumber);
        model.addAttribute("items", itemService.getAllItems(teamNumber));
        model.addAttribute("item", new Item());

        populateForm(model, teamNumber);
        return "items";
    }

    /** Adds a new inventory item.
     * 
     * @param item The item to add.
     * @param result The binding result for validation.
     * @param model The model to hold attributes for the view.
     * @param principal The principal representing the currently authenticated user.
     * @return The view name for displaying all items.
     */
    @PostMapping("/items")
    public String addItem(@Valid @ModelAttribute("item") Item item, BindingResult result, @RequestParam(required = false) List<Long> subteamIds, @RequestParam(defaultValue = "false") boolean sharedWithAll, Model model, Principal principal) {
        String teamNumber = principal.getName();

        if(!sharedWithAll && (subteamIds == null || subteamIds.isEmpty())) {
            result.reject("subteams.required", "Select at least one subteam or share with everyone.");
        }

        if (result.hasErrors()) {
            model.addAttribute("items", itemService.getAllItems(teamNumber));
            model.addAttribute("teamNumber", teamNumber);
            populateForm(model, teamNumber);

            return "items";
        }

        try {
            itemService.addItem(item, teamNumber, subteamIds, sharedWithAll);
        } catch (IllegalArgumentException ex) {
            result.rejectValue("sku", "duplicate", ex.getMessage());

            model.addAttribute("items", itemService.getAllItems(teamNumber));
            model.addAttribute("teamNumber", teamNumber);
            populateForm(model, teamNumber);

            return "items";
        }

        return "redirect:/items";
    }

    /** Displays the form for updating an existing item.
     * 
     * @param id The ID of the item to update.
     * @param model The model to hold attributes for the view.
     * @param principal The principal representing the currently authenticated user.
     * @return The view name for the update form.
     */
    @GetMapping("/items/{id}/update")
    public String showUpdateForm(@PathVariable Long id, Model model, Principal principal) {
        String teamNumber = principal.getName();
        Item item = itemService.getItemById(id, teamNumber);

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
     * @param principal The principal representing the currently authenticated user.
     * @return The view name for displaying all items.
     */
    @PostMapping("/items/{id}/update")
    public String updateItem(@PathVariable Long id, @Valid @ModelAttribute("item") Item updatedItem, BindingResult result, @RequestParam(required = false) List<Long> subteamIds, @RequestParam(defaultValue = "false") boolean sharedWithAll, Model model, Principal principal) {
        String teamNumber = principal.getName();
        Item existingItem = itemService.getItemById(id, teamNumber);

        if (existingItem == null) {
            return "redirect:/items";
        }

        if (!sharedWithAll && (subteamIds == null || subteamIds.isEmpty())) {
            result.reject("subteams.required", "Select at least one subteam or share with everyone.");
        }

        if (result.hasErrors()) {
            model.addAttribute("itemId", id);
            populateForm(model, teamNumber);
            return "updateItem";
        }

        try {
            itemService.updateItem(id, updatedItem, teamNumber, subteamIds, sharedWithAll);
        } catch (IllegalArgumentException ex) {
            result.rejectValue("sku", "duplicate", ex.getMessage());

            model.addAttribute("itemId", id);
            populateForm(model, teamNumber);

            return "updateItem";
        }

        return "redirect:/items";
    }

    /** Deletes an existing inventory item.
     * 
     * @param id The ID of the item to delete.
     * @param principal The principal representing the currently authenticated user.
     * @return The view name for displaying all items.
     */
    @PostMapping("/items/{id}/delete")
    public String deleteItem(@PathVariable Long id, Principal principal) {
        String teamNumber = principal.getName();
        itemService.deleteItem(id, teamNumber);
        return "redirect:/items";
    }

    /** Displays details for a specific inventory item.
     * 
     * @param id The ID of the item to display.
     * @param model The model to hold attributes for the view.
     * @param principal The principal representing the currently authenticated user.
     * @return The view name for displaying item details.
     */
    @GetMapping("/items/{id}")
    public String displayItemDetails(@PathVariable Long id, Model model, Principal principal) {
        String teamNumber = principal.getName();
        Item item = itemService.getItemById(id, teamNumber);

        if (item == null) {
            return "redirect:/items";
        }

        model.addAttribute("item", item);
        return "itemDetails";
    }
}
