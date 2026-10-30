package com.pittrack.pittrack.subteam;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pittrack.pittrack.account.TeamAccount;
import com.pittrack.pittrack.account.TeamAccountRepository;
import com.pittrack.pittrack.item.Item;

@Service
public class SubteamService {
    private final SubteamRepository subteamRepository;
    private final TeamAccountRepository teamAccountRepository;

    public SubteamService(SubteamRepository subteamRepository, TeamAccountRepository teamAccountRepository) {
        this.subteamRepository = subteamRepository;
        this.teamAccountRepository = teamAccountRepository;
    }

    public List<Subteam> getSubteams(String teamNumber) {
        return subteamRepository
            .findAllByTeamAccount_TeamNumberOrderByNameAsc(teamNumber);
    }

    @Transactional
    public Subteam createSubteam(String name, String description, String teamNumber) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "Subteam name is required."
            );
        }

        String cleanName = name.trim();

        if (cleanName.length() > 100) {
            throw new IllegalArgumentException(
                "Subteam name cannot exceed 100 characters."
            );
        }

        if (subteamRepository
                .existsByNameIgnoreCaseAndTeamAccount_TeamNumber(
                    cleanName, teamNumber)) {

            throw new IllegalArgumentException(
                "This subteam already exists."
            );
        }

        TeamAccount teamAccount = teamAccountRepository
            .findByTeamNumber(teamNumber)
            .orElseThrow(() ->
                new IllegalArgumentException("Team account not found.")
            );

        Subteam subteam = new Subteam();
        subteam.setName(cleanName);
        subteam.setDescription(
            description == null ? "" : description.trim()
        );
        subteam.setTeamAccount(teamAccount);

        return subteamRepository.save(subteam);
    }

    public boolean belongsToTeam(Long subteamId, String teamNumber) {
        return subteamRepository
            .existsByIdAndTeamAccount_TeamNumber(
                subteamId, teamNumber
            );
    }

    @Transactional
    public void deleteSubteam(Long subteamId, String teamNumber) {

        Subteam subteam = subteamRepository
            .findByIdAndTeamAccount_TeamNumber(subteamId, teamNumber)
            .orElseThrow(() ->
                new IllegalArgumentException("Subteam not found.")
            );

        // Remove the subteam association from its items.
        // The inventory items themselves are NOT deleted.
        for (Item item : subteam.getItems()) {
            item.getSubteams().remove(subteam);
        }

        subteamRepository.delete(subteam);
    }
}
