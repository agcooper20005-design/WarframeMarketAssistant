package com.aces.warframepersonalextractor.service;

import com.aces.warframepersonalextractor.dto.CreateOrderRequest;
import com.aces.warframepersonalextractor.dto.OrderAnalysisResponse;
import com.aces.warframepersonalextractor.dto.OrderPriceChangeResponse;
import com.aces.warframepersonalextractor.dto.OrderRefreshResult;
import com.aces.warframepersonalextractor.dto.OrderResponse;
import com.aces.warframepersonalextractor.dto.RefreshOrdersResponse;
import com.aces.warframepersonalextractor.external.dto.UpdateOrderRequest;

import com.aces.warframepersonalextractor.external.WarframeMarketClient;
import com.aces.warframepersonalextractor.external.dto.MarketCreateOrderRequest;
import com.aces.warframepersonalextractor.external.dto.MarketOrderApiResponse;
import com.aces.warframepersonalextractor.external.dto.MarketOrderResponse;
import com.aces.warframepersonalextractor.external.dto.MarketUpdateOrderRequest;
import com.aces.warframepersonalextractor.external.dto.market.MarketAnalysisResponse;
import com.aces.warframepersonalextractor.model.Item;
import com.aces.warframepersonalextractor.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarframeMarketOrderService {

    private final WarframeMarketClient warframeMarketClient;
    private final ItemRepository itemRepository;
    private final MarketAnalysisService marketAnalysisService;

    public List<OrderResponse> getMyOrders() {

        MarketOrderApiResponse response =
                warframeMarketClient.getMyOrders();

        if (
                response == null
                        || response.data() == null
        ) {
            return List.of();
        }

        return response.data()
                .stream()
                .map(this::mapOrder)
                .toList();
    }

    public String createOrder(
            CreateOrderRequest request
    ) {

        Item item = itemRepository
                .findByMarketItemId(
                        request.marketItemId()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Market item not found: "
                                        + request.marketItemId()
                        )
                );

        if (!Boolean.TRUE.equals(item.getTradable())) {
            throw new IllegalStateException(
                    "Item is not tradable."
            );
        }

        MarketCreateOrderRequest marketRequest =
                new MarketCreateOrderRequest(
                        item.getMarketItemId(),
                        request.type(),
                        request.platinum(),
                        request.quantity(),
                        request.visible(),

                        resolvePerTrade(
                                item,
                                request
                        ),

                        resolveRank(
                                item,
                                request
                        ),

                        request.charges(),
                        request.subtype(),
                        request.amberStars(),
                        request.cyanStars()
                );

        return warframeMarketClient
                .createOrder(
                        marketRequest
                );
    }

    public void deleteOrder(
            String orderId
    ) {

        warframeMarketClient.deleteOrder(
                orderId
        );
    }

    public String updateOrder(
            String orderId,
            UpdateOrderRequest request
    ) {

        MarketUpdateOrderRequest marketRequest =
                new MarketUpdateOrderRequest(
                        request.platinum(),
                        request.quantity(),
                        request.visible(),
                        request.perTrade(),
                        request.rank()
                );

        return warframeMarketClient
                .updateOrder(
                        orderId,
                        marketRequest
                );
    }

    public OrderAnalysisResponse analyzeOrder(
            String orderId
    ) {

        MarketOrderResponse order =
                getOrderById(orderId);

        MarketAnalysisResponse analysis =
                marketAnalysisService.analyzeItem(
                        order.itemId()
                );

        return new OrderAnalysisResponse(
                order.id(),
                getItemName(order.itemId()),
                order.platinum(),
                analysis.lowestSell(),
                analysis.highestBuy(),
                analysis.suggestedSell(),
                analysis.reason()
        );
    }

    public OrderPriceChangeResponse applySuggestedPrice(
            String orderId
    ) {

        MarketOrderResponse order =
                getOrderById(orderId);

        MarketAnalysisResponse analysis =
                marketAnalysisService.analyzeItem(
                        order.itemId()
                );

        Integer suggestedPrice =
                analysis.suggestedSell();

        if (suggestedPrice == null) {
            throw new IllegalStateException(
                    "No suggested sell price was available."
            );
        }

        MarketUpdateOrderRequest request =
                new MarketUpdateOrderRequest(
                        suggestedPrice,
                        null,
                        null,
                        null,
                        null
                );

        warframeMarketClient.updateOrder(
                orderId,
                request
        );

        return new OrderPriceChangeResponse(
                order.id(),
                getItemName(order.itemId()),
                order.platinum(),
                suggestedPrice,
                analysis.reason()
        );
    }

    public RefreshOrdersResponse refreshAllOrders() {

        MarketOrderApiResponse response =
                warframeMarketClient.getMyOrders();

        if (
                response == null
                        || response.data() == null
        ) {

            return new RefreshOrdersResponse(
                    0,
                    0,
                    0,
                    0,
                    List.of()
            );
        }

        List<OrderRefreshResult> results =
                new ArrayList<>();

        int changed = 0;
        int unchanged = 0;
        int skipped = 0;

        for (MarketOrderResponse order :
                response.data()) {

            /*
             * For now, only SELL orders use
             * the suggested sell algorithm.
             */
            if (!"sell".equalsIgnoreCase(
                    order.type()
            )) {

                results.add(
                        new OrderRefreshResult(
                                order.id(),
                                getItemName(
                                        order.itemId()
                                ),
                                order.platinum(),
                                null,
                                false,
                                "Skipped: buy-order pricing is not implemented yet."
                        )
                );

                skipped++;

                continue;
            }

            MarketAnalysisResponse analysis;

            try {

                analysis =
                        marketAnalysisService
                                .analyzeItem(
                                        order.itemId()
                                );

            } catch (Exception e) {

                results.add(
                        new OrderRefreshResult(
                                order.id(),
                                getItemName(
                                        order.itemId()
                                ),
                                order.platinum(),
                                null,
                                false,
                                "Analysis failed: "
                                        + e.getMessage()
                        )
                );

                skipped++;

                continue;
            }

            Integer suggestedPrice =
                    analysis.suggestedSell();

            /*
             * We cannot refresh this listing if
             * the analyzer cannot determine a price.
             */
            if (suggestedPrice == null) {

                results.add(
                        new OrderRefreshResult(
                                order.id(),
                                getItemName(
                                        order.itemId()
                                ),
                                order.platinum(),
                                null,
                                false,
                                "Skipped: no suggested sell price available."
                        )
                );

                skipped++;

                continue;
            }

            /*
             * IMPORTANT:
             *
             * Even if the current price already equals
             * the suggested price, we STILL PATCH it.
             *
             * The purpose of refresh is not only to
             * change prices. It also refreshes the
             * existing listing so it can remain current.
             */
            boolean priceChanged =
                    !suggestedPrice.equals(
                            order.platinum()
                    );

            MarketUpdateOrderRequest updateRequest =
                    new MarketUpdateOrderRequest(
                            suggestedPrice,
                            null,
                            null,
                            null,
                            null
                    );

            try {

                warframeMarketClient.updateOrder(
                        order.id(),
                        updateRequest
                );

                if (priceChanged) {

                    results.add(
                            new OrderRefreshResult(
                                    order.id(),
                                    getItemName(
                                            order.itemId()
                                    ),
                                    order.platinum(),
                                    suggestedPrice,
                                    true,
                                    analysis.reason()
                            )
                    );

                    changed++;

                } else {

                    /*
                     * Same price, but the PATCH was
                     * intentionally still sent.
                     */
                    results.add(
                            new OrderRefreshResult(
                                    order.id(),
                                    getItemName(
                                            order.itemId()
                                    ),
                                    order.platinum(),
                                    suggestedPrice,
                                    false,
                                    "Price remains optimal. Listing refreshed."
                            )
                    );

                    unchanged++;
                }

            } catch (Exception e) {

                results.add(
                        new OrderRefreshResult(
                                order.id(),
                                getItemName(
                                        order.itemId()
                                ),
                                order.platinum(),
                                suggestedPrice,
                                false,
                                "Refresh failed: "
                                        + e.getMessage()
                        )
                );

                skipped++;
            }
        }

        return new RefreshOrdersResponse(
                response.data().size(),
                changed,
                unchanged,
                skipped,
                results
        );
    }

    private MarketOrderResponse getOrderById(
            String orderId
    ) {

        MarketOrderApiResponse response =
                warframeMarketClient.getMyOrders();

        if (
                response == null
                        || response.data() == null
        ) {
            throw new IllegalStateException(
                    "Warframe.Market returned no orders."
            );
        }

        return response.data()
                .stream()
                .filter(order ->
                        order.id().equals(orderId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Order not found: "
                                        + orderId
                        )
                );
    }

    private String getItemName(
            String marketItemId
    ) {

        return itemRepository
                .findByMarketItemId(
                        marketItemId
                )
                .map(Item::getName)
                .orElse("Unknown Item");
    }

    private Integer resolvePerTrade(
            Item item,
            CreateOrderRequest request
    ) {

        if (!Boolean.TRUE.equals(
                item.getBulkTradable()
        )) {
            return null;
        }

        return request.perTrade();
    }

    private Integer resolveRank(
            Item item,
            CreateOrderRequest request
    ) {

        if (
                item.getMaxRank() == null
                        || item.getMaxRank() <= 0
        ) {
            return null;
        }

        return request.rank();
    }

    private OrderResponse mapOrder(
            MarketOrderResponse order
    ) {

        return new OrderResponse(
                order.id(),
                order.type(),
                order.platinum(),
                order.quantity(),
                order.rank(),
                order.visible(),
                order.itemId(),
                getItemName(
                        order.itemId()
                )
        );
    }
}