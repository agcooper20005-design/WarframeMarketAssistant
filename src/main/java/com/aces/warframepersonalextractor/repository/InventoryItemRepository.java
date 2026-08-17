package com.aces.warframepersonalextractor.repository;

import com.aces.warframepersonalextractor.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository
        extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByMarketItemIdAndRank(
            String marketItemId,
            Integer rank
    );

    Optional<InventoryItem> findByGameRefAndRank(
            String gameRef,
            Integer rank
    );

    List<InventoryItem> findAllByOrderByNameAsc();
}