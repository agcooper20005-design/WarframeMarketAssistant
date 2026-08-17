package com.aces.warframepersonalextractor.controller;

import com.aces.warframepersonalextractor.model.Item;
import com.aces.warframepersonalextractor.service.ItemSyncService;
import com.aces.warframepersonalextractor.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final ItemSyncService itemSyncService;

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Integer>> syncItems() {

        int added = itemSyncService.syncItems();

        return ResponseEntity.ok(
                Map.of("added", added)
        );
    }

    @GetMapping("/tradable/search")
    public ResponseEntity<List<Item>> searchTradableItems(@RequestParam String q) {
        System.out.println("searching for " + q);
        return ResponseEntity.ok(itemService.searchTradableItems(q));

    }
}