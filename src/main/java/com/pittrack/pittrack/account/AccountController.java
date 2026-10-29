package com.pittrack.pittrack.account;

import java.security.Principal;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AccountController {
    private final TeamAccountService teamAccountService;

    public AccountController(TeamAccountService teamAccountService) {
        this.teamAccountService = teamAccountService;
    }

    /** Displays the home page.
     * 
     * @return the view name.
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    /** Displays the login page.
     * 
     * @return the view name.
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /** Displays the registration page.
     * 
     * @return the view name.
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    /** Creates a new team account.
     * 
     * @param teamNumber the team number.
     * @param password the password.
     * @param model the model.
     * @param redirectAttributes the redirect attributes.
     * @return the view name.
     */
    @PostMapping("/register")
    public String createAccount(@RequestParam String teamNumber, @RequestParam String password, Model model, RedirectAttributes redirectAttributes) {
        try {
            teamAccountService.register(teamNumber, password);
            redirectAttributes.addFlashAttribute("success", "Account created successfully. Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("teamNumber", teamNumber);
            return "register";
        }
    }

    /**
     * Deletes the currently authenticated team's account.
     *
     * @param principal the currently authenticated user
     * @param request the HTTP request
     * @param redirectAttributes the redirect attributes
     * @return the redirect view
     */
    @PostMapping("/account/delete")
    public String deleteAccount(
            Principal principal,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        String teamNumber = principal.getName();

        teamAccountService.deleteAccount(teamNumber);

        // Clear the security context
        SecurityContextHolder.clearContext();

        // Invalidate the current session
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        redirectAttributes.addFlashAttribute(
            "success",
            "Your team account has been deleted successfully."
        );

        return "redirect:/login";
    }
}
