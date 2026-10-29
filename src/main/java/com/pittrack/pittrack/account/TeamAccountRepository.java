package com.pittrack.pittrack.account;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamAccountRepository extends JpaRepository<TeamAccount, Long> {
    Optional<TeamAccount> findByTeamNumber(String teamNumber);
    boolean existsByTeamNumber(String teamNumber);

}
