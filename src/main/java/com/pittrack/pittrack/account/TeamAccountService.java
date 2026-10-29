package com.pittrack.pittrack.account;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamAccountService {
    private final TeamAccountRepository repository;
    private final PasswordEncoder passwordEncoder;

    public TeamAccountService(TeamAccountRepository teamAccountRepository, PasswordEncoder passwordEncoder) {
        this.repository = teamAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Registers a new team account.
     * 
     * @param teamNumber the team number
     * @param password the password
     * @throws IllegalArgumentException if the team number is invalid, the password is too short, or an account with the same team number already exists.
     */
    @Transactional
    public void register(String teamNumber, String password) {
        String team = teamNumber == null ? null : teamNumber.trim();

        if (!team.matches("[0-9]{1,10}")) {
            throw new IllegalArgumentException("Enter a valid team number.");
        }

        if (password == null || password.length() < 12) {
            throw new IllegalArgumentException("Password must be at least 12 characters long.");
        }

        if (repository.existsByTeamNumber(team)) {
            throw new IllegalArgumentException("An account with this team number already exists.");
        }

        String hash = passwordEncoder.encode(password);
        repository.save(new TeamAccount(team, hash));
    }
}
