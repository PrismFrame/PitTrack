
package com.pittrack.pittrack.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pittrack.pittrack.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);
}
