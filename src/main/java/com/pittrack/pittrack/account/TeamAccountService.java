package com.pittrack.pittrack.account;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pittrack.pittrack.item.ItemRepository;
import com.pittrack.pittrack.subteam.SubteamRepository;

@Service
public class TeamAccountService {
    private final TeamAccountRepository repository;
    private final SubteamRepository subteamRepository;
    private final ItemRepository itemRepository;
    private final PasswordEncoder passwordEncoder;

    public TeamAccountService(TeamAccountRepository teamAccountRepository, SubteamRepository subteamRepository, ItemRepository itemRepository, PasswordEncoder passwordEncoder) {
        this.repository = teamAccountRepository;
        this.subteamRepository = subteamRepository;
        this.passwordEncoder = passwordEncoder;
        this.itemRepository = itemRepository;
    }

    /** Registers a new team account.
     * 
     * @param teamNumber the team number
     * @param password the password
     * @throws IllegalArgumentException if the team number is invalid, the password is too short, or an account with the same team number already exists.
     */
    @Transactional
    public void register(String teamNumber, String password) {
        if (teamNumber == null || !teamNumber.matches("[0-9]{1,10}")) {
            throw new IllegalArgumentException("Enter a valid team number.");
        }

        if (password == null || password.length() < 12) {
            throw new IllegalArgumentException(
                "Password must be at least 12 characters long."
            );
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                "Password cannot exceed 72 UTF-8 bytes."
            );
        }

        if (repository.existsByTeamNumber(teamNumber)) {
            throw new IllegalArgumentException("An account with this team number already exists.");
        }

        String hash = passwordEncoder.encode(password);
        repository.save(new TeamAccount(teamNumber, hash));
    }

    /**
     * Deletes a team account.
     *
     * @param teamNumber the team number
     */
    @Transactional
    public void deleteAccount(String teamNumber) {

        if (teamNumber == null || teamNumber.isBlank()) {
            throw new IllegalArgumentException(
                "Invalid team number."
            );
        }

        TeamAccount account = repository.findByTeamNumber(teamNumber)
            .orElseThrow(() ->
                new IllegalArgumentException("Team account not found.")
            );

        // Delete inventory first.
        itemRepository.deleteByTeamAccount_TeamNumber(teamNumber);

        // Delete subteams.
        subteamRepository.deleteByTeamAccount_TeamNumber(teamNumber);

        // Delete the team account.
        repository.delete(account);
    }
}
