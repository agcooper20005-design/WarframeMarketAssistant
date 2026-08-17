package com.aces.warframepersonalextractor.controller;

import com.aces.warframepersonalextractor.model.InventoryItem;
import com.aces.warframepersonalextractor.repository.InventoryItemRepository;
import com.aces.warframepersonalextractor.service.InventoryImportService;
import com.aces.warframepersonalextractor.service.InventoryParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryImportService inventoryImportService;
    private final InventoryParserService inventoryParserService;
    private final InventoryItemRepository inventoryItemRepository;

    @PostMapping("/import")
    public ResponseEntity<Void> importInventory() {
        System.out.println("importing inventory");
        String inventoryJson =
                inventoryImportService.importInventory();

        System.out.println("Parsing inventory:");
        inventoryParserService.parseAndStore(inventoryJson);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<InventoryItem>> getInventory() {

        return ResponseEntity.ok(
                inventoryItemRepository.findAll()
        );
    }
}