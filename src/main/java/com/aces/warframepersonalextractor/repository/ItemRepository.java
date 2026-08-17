package com.aces.warframepersonalextractor.repository;

import com.aces.warframepersonalextractor.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByMarketItemId(String marketItemId);

    Optional<Item> findByGameRef(String gameRef);

    List<Item> findByTradableTrueAndNameContainingIgnoreCase(String name);


    boolean existsByMarketItemId(String marketItemId);
}