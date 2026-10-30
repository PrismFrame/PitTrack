package com.pittrack.pittrack.dashboard;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.pittrack.pittrack.subteam.SubteamService;

@Controller
public class DashboardController {
    private final DashboardService dashboardService;
    private final SubteamService subteamService;

    public DashboardController(DashboardService dashboardService, SubteamService subteamService) {
        this.dashboardService = dashboardService;
        this.subteamService = subteamService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Long subteamId, Principal principal, Model model) {
        String teamNumber = principal.getName();

        model.addAttribute(
            "stats",
            dashboardService.getStats(teamNumber, subteamId)
        );

        var subteams = subteamService.getSubteams(teamNumber);

        model.addAttribute("subteams", subteams);

        model.addAttribute(
            "selectedSubteamId",
            subteamId
        );

        model.addAttribute(
            "selectedSubteam",
            subteamId == null ? null :
                subteams.stream()
                    .filter(s -> s.getId().equals(subteamId))
                    .findFirst()
                    .orElse(null)
        );

        model.addAttribute(
            "selectedSubteamId",
            subteamId
        );

        model.addAttribute(
            "teamNumber",
            teamNumber
        );

        return "dashboard";
    }
}
