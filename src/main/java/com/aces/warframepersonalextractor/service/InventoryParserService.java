package com.aces.warframepersonalextractor.service;

import com.aces.warframepersonalextractor.model.InventoryItem;
import com.aces.warframepersonalextractor.model.enums.RelicRefinement;
import com.aces.warframepersonalextractor.repository.InventoryItemRepository;
import com.aces.warframepersonalextractor.repository.ItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryParserService {

    private final InventoryItemRepository inventoryItemRepository;
    private final ItemRepository itemRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void parseAndStore(String inventoryJson) {

        try {

            JsonNode root =
                    objectMapper.readTree(inventoryJson);

            parseStackableItems(root);

            parseMods(root);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to parse Warframe inventory JSON.",
                    e
            );
        }
    }

    public void storeInventoryItem(
            String gameRef,
            Integer rank,
            int quantity
    ) {

        RelicRefinement refinement =
                extractRelicRefinement(gameRef);

        Optional<InventoryItem> existing =
                inventoryItemRepository.findByGameRefAndRank(
                        gameRef,
                        rank
                );

        InventoryItem inventoryItem;

        if (existing.isPresent()) {

            inventoryItem =
                    existing.get();

        } else {

            inventoryItem =
                    new InventoryItem();

            inventoryItem.setGameRef(gameRef);
            inventoryItem.setRank(rank);
        }

        inventoryItem.setQuantity(quantity);
        inventoryItem.setRefinement(refinement);

        mapMarketItem(
                inventoryItem,
                gameRef
        );

        inventoryItemRepository.save(
                inventoryItem
        );
    }

    private void mapMarketItem(
            InventoryItem inventoryItem,
            String gameRef
    ) {

        String marketLookupGameRef =
                normalizeRelicGameRef(gameRef);

        itemRepository.findByGameRef(
                        marketLookupGameRef
                )
                .ifPresent(marketItem -> {

                    inventoryItem.setMarketItemId(
                            marketItem.getMarketItemId()
                    );

                    inventoryItem.setName(
                            marketItem.getName()
                    );
                });
    }

    private String normalizeRelicGameRef(
            String gameRef
    ) {

        if (gameRef == null) {
            return null;
        }

        if (!gameRef.contains("/Projections/")) {
            return gameRef;
        }

        if (gameRef.endsWith("Bronze")) {

            return gameRef.substring(
                    0,
                    gameRef.length()
                            - "Bronze".length()
            );
        }

        if (gameRef.endsWith("Silver")) {

            return gameRef.substring(
                    0,
                    gameRef.length()
                            - "Silver".length()
            );
        }

        if (gameRef.endsWith("Gold")) {

            return gameRef.substring(
                    0,
                    gameRef.length()
                            - "Gold".length()
            );
        }

        if (gameRef.endsWith("Platinum")) {

            return gameRef.substring(
                    0,
                    gameRef.length()
                            - "Platinum".length()
            );
        }

        return gameRef;
    }

    private RelicRefinement extractRelicRefinement(
            String gameRef
    ) {

        if (gameRef == null
                || !gameRef.contains("/Projections/")) {

            return null;
        }

        if (gameRef.endsWith("Bronze")) {
            return RelicRefinement.INTACT;
        }

        if (gameRef.endsWith("Silver")) {
            return RelicRefinement.EXCEPTIONAL;
        }

        if (gameRef.endsWith("Gold")) {
            return RelicRefinement.FLAWLESS;
        }

        if (gameRef.endsWith("Platinum")) {
            return RelicRefinement.RADIANT;
        }

        return null;
    }

    private void parseStackableItems(
            JsonNode node
    ) {

        if (node.isObject()) {

            if (
                    node.has("ItemType")
                            && node.has("ItemCount")
            ) {

                String gameRef =
                        node.get("ItemType")
                                .asText();

                int quantity =
                        node.get("ItemCount")
                                .asInt();

                storeInventoryItem(
                        gameRef,
                        null,
                        quantity
                );
            }

            node.fields()
                    .forEachRemaining(
                            entry ->
                                    parseStackableItems(
                                            entry.getValue()
                                    )
                    );

        } else if (node.isArray()) {

            for (JsonNode child : node) {

                parseStackableItems(
                        child
                );
            }
        }
    }

    private void parseMods(
            JsonNode root
    ) {

        JsonNode upgrades =
                root.get("Upgrades");

        if (
                upgrades == null
                        || !upgrades.isArray()
        ) {
            return;
        }

        Map<ModKey, Integer> modCounts =
                new HashMap<>();

        for (JsonNode mod : upgrades) {

            if (!mod.has("ItemType")) {
                continue;
            }

            String gameRef =
                    mod.get("ItemType")
                            .asText();

            Integer rank =
                    extractModRank(mod);

            ModKey key =
                    new ModKey(
                            gameRef,
                            rank
                    );

            modCounts.merge(
                    key,
                    1,
                    Integer::sum
            );
        }

        for (
                Map.Entry<ModKey, Integer> entry
                : modCounts.entrySet()
        ) {

            ModKey key =
                    entry.getKey();

            int quantity =
                    entry.getValue();

            storeInventoryItem(
                    key.gameRef(),
                    key.rank(),
                    quantity
            );
        }
    }

    private Integer extractModRank(
            JsonNode mod
    ) {

        JsonNode fingerprintNode =
                mod.get(
                        "UpgradeFingerprint"
                );

        if (
                fingerprintNode == null
                        || fingerprintNode.isNull()
                        || fingerprintNode
                        .asText()
                        .isBlank()
        ) {

            return 0;
        }

        try {

            JsonNode fingerprint =
                    objectMapper.readTree(
                            fingerprintNode.asText()
                    );

            return fingerprint
                    .path("lvl")
                    .asInt(0);

        } catch (Exception e) {

            return 0;
        }
    }

    private record ModKey(
            String gameRef,
            Integer rank
    ) {
    }
}