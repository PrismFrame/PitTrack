package com.pittrack.pittrack.account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
}
