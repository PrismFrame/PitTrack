
package com.pittrack.pittrack.item;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByTeamAccount_TeamNumber(String teamNumber);
    Optional<Item> findByIdAndTeamAccount_TeamNumber(Long id, String teamNumber);
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);
}
