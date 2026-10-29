package com.pittrack.pittrack.account;

import jakarta.persistence.*;

@Entity
@Table(name = "team_accounts")
public class TeamAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_number", nullable = false, unique = true)
    private String teamNumber;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    protected TeamAccount() {}

    public TeamAccount(String teamNumber, String passwordHash) {
        this.teamNumber = teamNumber;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public String getTeamNumber() {
        return teamNumber;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
