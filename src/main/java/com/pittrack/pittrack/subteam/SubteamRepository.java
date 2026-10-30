package com.pittrack.pittrack.subteam;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubteamRepository extends JpaRepository<Subteam, Long> {
    List<Subteam> findAllByTeamAccount_TeamNumberOrderByNameAsc(String teamNumber);
    boolean existsByNameIgnoreCaseAndTeamAccount_TeamNumber(String name, String teamNumber);
    boolean existsByIdAndTeamAccount_TeamNumber(Long id, String teamNumber);
    List<Subteam> findAllByIdInAndTeamAccount_TeamNumber(List<Long> uniqueIds, String teamNumber);
    Optional<Subteam> findByIdAndTeamAccount_TeamNumber(Long id, String teamNumber);
}
