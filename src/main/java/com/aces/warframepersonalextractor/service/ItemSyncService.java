package com.aces.warframepersonalextractor.service;

import com.aces.warframepersonalextractor.external.WarframeMarketClient;
import com.aces.warframepersonalextractor.external.dto.ItemApiResponse;
import com.aces.warframepersonalextractor.external.dto.MarketItemResponse;
import com.aces.warframepersonalextractor.model.Item;
import com.aces.warframepersonalextractor.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemSyncService {

    private final WarframeMarketClient warframeMarketClient;
    private final ItemRepository itemRepository;

    @Transactional
    public int syncItems() {

        ItemApiResponse response =
                warframeMarketClient.getAllItems();

        int addedCount = 0;

        for (MarketItemResponse marketItem : response.data()) {

            if (itemRepository.existsByMarketItemId(marketItem.id())) {
                continue;
            }

            Item item = new Item();

            item.setMarketItemId(marketItem.id());
            item.setSlug(marketItem.slug());
            item.setName(marketItem.i18n().get("en").name());
            item.setGameRef(marketItem.gameRef());
            item.setMaxRank(marketItem.maxRank());
            item.setTradable(true);
            item.setBulkTradable(marketItem.bulkTradable());
            item.setRarity(marketItem.rarity());

            if (marketItem.tags() != null) {
                item.getTags().addAll(marketItem.tags());
            }

            itemRepository.save(item);

            addedCount++;
        }

        return addedCount;
    }
}