package com.pittrack.pittrack.subteam;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/subteams")
public class SubteamController {
    private final SubteamService subteamService;

    public SubteamController(SubteamService subteamService) {
        this.subteamService = subteamService;
    }

    @GetMapping
    public String listSubteams(Principal principal, Model model) {
        model.addAttribute(
            "subteams",
            subteamService.getSubteams(principal.getName())
        );

        return "subteams";
    }

    @PostMapping
    public String createSubteam(@RequestParam String name, @RequestParam(required = false) String description, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            subteamService.createSubteam(
                name,
                description,
                principal.getName()
            );

            redirectAttributes.addFlashAttribute(
                "successMessage",
                "Subteam created successfully!"
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                "errorMessage",
                ex.getMessage()
            );
        }

        return "redirect:/subteams";
    }

    @PostMapping("/{id}/delete")
    public String deleteSubteam(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            subteamService.deleteSubteam(
                id,
                principal.getName()
            );

            redirectAttributes.addFlashAttribute(
                "successMessage",
                "Subteam deleted successfully."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                "errorMessage",
                ex.getMessage()
            );
        }

        return "redirect:/subteams";
    }
}
