package com.aces.warframepersonalextractor.service;

import com.aces.warframepersonalextractor.dto.CreateOrderRequest;
import com.aces.warframepersonalextractor.model.Item;
import com.aces.warframepersonalextractor.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final WarframeMarketOrderService warframeMarketOrderService;

    public List<Item> searchTradableItems(String search){

        return itemRepository.findByTradableTrueAndNameContainingIgnoreCase(search);

    }

    public String createOrder(CreateOrderRequest request) {

        Item item = itemRepository
                .findByMarketItemId(request.marketItemId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Item not found: " + request.marketItemId()
                        )
                );

        return warframeMarketOrderService.createOrder(request);


    }


}
