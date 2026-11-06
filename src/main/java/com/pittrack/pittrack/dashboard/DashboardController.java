package com.pittrack.pittrack.dashboard;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.pittrack.pittrack.subteam.Subteam;
import com.pittrack.pittrack.subteam.SubteamService;

@Controller
public class DashboardController {
    private final DashboardService dashboardService;
    private final SubteamService subteamService;

    public DashboardController(DashboardService dashboardService, SubteamService subteamService) {
        this.dashboardService = dashboardService;
        this.subteamService = subteamService;
    }

    /**
     * Displays the dashboard for the user.
     *
     * @param subteamId the ID of the subteam to display (optional)
     * @param principal the authenticated user
     * @param model the model for the view
     * @return the name of the view to display
     */
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Long subteamId, Principal principal, Model model) {
        String teamNumber = principal.getName();
        var subteams = subteamService.getSubteams(teamNumber);
        Subteam selectedSubteam = null;

        if (subteamId != null) {
            selectedSubteam = subteams.stream()
                .filter(s -> s.getId().equals(subteamId))
                .findFirst()
                .orElse(null);

            // Reject subteams that do not belong to this team.
            if (selectedSubteam == null) {
                return "redirect:/dashboard";
            }
        }

        model.addAttribute(
            "stats",
            dashboardService.getStats(teamNumber, subteamId)
        );

        model.addAttribute("subteams", subteams);
        model.addAttribute("selectedSubteam", selectedSubteam);
        model.addAttribute("selectedSubteamId", subteamId);
        model.addAttribute("teamNumber", teamNumber);

        return "dashboard";
    }
}
