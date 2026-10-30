package com.pittrack.pittrack.item;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pittrack.pittrack.subteam.Subteam;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByTeamAccount_TeamNumber(String teamNumber);
    Optional<Item> findByIdAndTeamAccount_TeamNumber(Long id, String teamNumber);
    boolean existsBySkuAndTeamAccount_TeamNumber(String sku, String teamNumber);
    boolean existsBySkuAndTeamAccount_TeamNumberAndIdNot(String sku, String teamNumber, Long id);
    long deleteByTeamAccount_TeamNumber(String teamNumber);
    @Query("""
        SELECT DISTINCT i
        FROM Item i
        LEFT JOIN i.subteams s
        WHERE i.teamAccount.teamNumber = :teamNumber
        AND (
            i.sharedWithAll = true
            OR s.id = :subteamId
        )
        """)
    List<Item> findVisibleToSubteam(
        @Param("teamNumber") String teamNumber,
        @Param("subteamId") Long subteamId
    );
    List<Subteam> findAllByIdInAndTeamAccount_TeamNumber(List<Long> ids, String teamNumber);
}
