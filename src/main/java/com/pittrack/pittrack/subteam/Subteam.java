package com.pittrack.pittrack.subteam;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

import com.pittrack.pittrack.account.TeamAccount;
import com.pittrack.pittrack.item.Item;

@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"team_account_id", "name"}
        )
    }
)
public class Subteam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_account_id", nullable = false)
    private TeamAccount teamAccount;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "subteams")
    private Set<Item> items = new HashSet<>();

    public Subteam() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public TeamAccount getTeamAccount() {
        return teamAccount;
    }

    public void setTeamAccount(TeamAccount teamAccount) {
        this.teamAccount = teamAccount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Set<Item> getItems() {
        return items;
    }
}
