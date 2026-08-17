package com.aces.warframepersonalextractor.service;

import com.aces.warframepersonalextractor.external.WarframeMarketClient;
import com.aces.warframepersonalextractor.external.dto.MarketOrderResponse;
import com.aces.warframepersonalextractor.external.dto.MarketOrdersApiResponse;
import com.aces.warframepersonalextractor.external.dto.market.MarketAnalysisResponse;
import com.aces.warframepersonalextractor.external.dto.market.MarketListingSnapshot;
import com.aces.warframepersonalextractor.model.Item;
import com.aces.warframepersonalextractor.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketAnalysisService {

    private static final int LISTING_PREVIEW_LIMIT = 20;

    private final WarframeMarketClient warframeMarketClient;
    private final ItemRepository itemRepository;

    public MarketAnalysisResponse analyzeItem(
            String marketItemId
    ) {

        Item item = itemRepository
                .findByMarketItemId(
                        marketItemId
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Item not found: "
                                        + marketItemId
                        )
                );

        MarketOrdersApiResponse response =
                warframeMarketClient
                        .getOrdersForItem(
                                marketItemId
                        );

        if (
                response == null
                        || response.data() == null
        ) {

            throw new IllegalStateException(
                    "Warframe.Market returned no order data."
            );
        }

        /*
         * Only IN-GAME users are currently relevant
         * to our market analysis.
         *
         * Online and offline users are deliberately
         * excluded.
         */
        List<MarketOrderResponse> inGameOrders =
                response.data()
                        .stream()
                        .filter(this::isInGame)
                        .toList();

        /*
         * Sell listings:
         *
         * Cheapest first.
         */
        List<MarketOrderResponse> sells =
                inGameOrders
                        .stream()
                        .filter(order ->
                                "sell".equalsIgnoreCase(
                                        order.type()
                                )
                        )
                        .sorted(
                                Comparator.comparingInt(
                                        MarketOrderResponse::platinum
                                )
                        )
                        .toList();

        /*
         * Buy listings:
         *
         * Highest offer first.
         */
        List<MarketOrderResponse> buys =
                inGameOrders
                        .stream()
                        .filter(order ->
                                "buy".equalsIgnoreCase(
                                        order.type()
                                )
                        )
                        .sorted(
                                Comparator.comparingInt(
                                        MarketOrderResponse::platinum
                                ).reversed()
                        )
                        .toList();

        Integer lowestSell =
                sells.isEmpty()
                        ? null
                        : sells.getFirst()
                        .platinum();

        Integer highestBuy =
                buys.isEmpty()
                        ? null
                        : buys.getFirst()
                        .platinum();

        /*
         * VERSION 2 returns a full decision object
         * rather than only an Integer.
         *
         * That gives us:
         *
         * - suggested price
         * - confidence
         * - explanation
         */
        SellSuggestion sellSuggestion =
                calculateSuggestedSell(
                        sells
                );

        Integer suggestedSell =
                sellSuggestion.price();

        Integer confidence =
                sellSuggestion.confidence();

        String confidenceLabel =
                confidenceLabel(
                        confidence
                );

        return new MarketAnalysisResponse(
                item.getMarketItemId(),
                item.getName(),

                lowestSell,
                highestBuy,
                suggestedSell,

                confidence,
                confidenceLabel,

                sells.size(),
                buys.size(),

                toSnapshots(
                        sells
                ),

                toSnapshots(
                        buys
                ),

                sellSuggestion.reason()
        );
    }

    private boolean isInGame(
            MarketOrderResponse order
    ) {

        return order.user() != null
                && order.user().status() != null
                && "ingame".equalsIgnoreCase(
                order.user().status()
        );
    }

    private List<MarketListingSnapshot> toSnapshots(
            List<MarketOrderResponse> orders
    ) {

        return orders
                .stream()
                .limit(
                        LISTING_PREVIEW_LIMIT
                )
                .map(order ->
                        new MarketListingSnapshot(
                                order.id(),

                                order.user() != null
                                        ? order.user()
                                        .ingameName()
                                        : "Unknown",

                                order.type(),
                                order.platinum(),
                                order.quantity(),
                                order.rank()
                        )
                )
                .toList();
    }

    /*
     * Converts the raw listing book into price levels.
     *
     * Example:
     *
     * Raw:
     *
     * 20
     * 20
     * 20
     * 21
     * 22
     * 22
     *
     * Becomes:
     *
     * 20 -> 3 sellers
     * 21 -> 1 seller
     * 22 -> 2 sellers
     *
     * IMPORTANT:
     *
     * We count sellers/listings, NOT quantity.
     *
     * Someone selling quantity 50 is still one seller
     * occupying that market position.
     */
    private List<PriceLevel> buildPriceLevels(
            List<MarketOrderResponse> sells
    ) {

        Map<Integer, Long> grouped =
                sells.stream()
                        .collect(
                                Collectors.groupingBy(
                                        MarketOrderResponse::platinum,
                                        TreeMap::new,
                                        Collectors.counting()
                                )
                        );

        return grouped.entrySet()
                .stream()
                .map(entry ->
                        new PriceLevel(
                                entry.getKey(),
                                entry.getValue()
                        )
                )
                .toList();
    }

    /*
     * =========================================================
     * MARKET ALGORITHM
     * =========================================================
     *
     * VERSION 2
     *
     * Primary goal:
     *
     * Stay close enough to the front of the market to actually
     * sell without blindly following every individual undercut.
     *
     * We are NOT trying to:
     *
     * - always pick the absolute lowest price
     * - always pick the largest price cluster
     * - maximize theoretical platinum
     *
     * We ARE trying to identify the believable active market.
     *
     *
     * V2 understands:
     *
     * 1. Supported floors.
     * 2. Isolated undercuts.
     * 3. Unrealistically high price clusters.
     * 4. Downward pricing staircases.
     * 5. Sparse markets.
     * 6. Number of sellers ahead of us.
     */
    private SellSuggestion calculateSuggestedSell(
            List<MarketOrderResponse> sells
    ) {

        if (sells.isEmpty()) {

            return new SellSuggestion(
                    null,
                    0,
                    "No in-game sell listings were found."
            );
        }

        List<PriceLevel> levels =
                buildPriceLevels(
                        sells
                );

        PriceLevel lowestLevel =
                levels.getFirst();

        int lowestPrice =
                lowestLevel.price();

        /*
         * =====================================================
         * CASE 1
         * SUPPORTED ABSOLUTE FLOOR
         * =====================================================
         *
         * Example:
         *
         * 28
         * 28
         * 29
         * 30
         * 35
         *
         * Two sellers already agree on 28.
         *
         * There is no reason to undercut them.
         *
         * Suggest:
         *
         * 28
         */
        if (lowestLevel.listings() >= 2) {

            int confidence =
                    calculateConfidence(
                            lowestPrice,
                            sells,
                            levels
                    );

            return new SellSuggestion(
                    lowestPrice,
                    confidence,
                    "The lowest in-game sell price is supported by multiple sellers. "
                            + "The market floor appears stable."
            );
        }

        /*
         * =====================================================
         * CASE 2
         * SPARSE MARKET
         * =====================================================
         *
         * Example:
         *
         * 20
         * 30
         * 50
         * 50
         *
         * The duplicate 50p listing does NOT mean 50p is
         * a believable current sell position.
         *
         * There are simply too few sellers to establish
         * a reliable high-price floor.
         *
         * Suggest:
         *
         * 20
         *
         * Confidence should naturally be lower.
         */
        if (sells.size() <= 5) {

            int confidence =
                    calculateConfidence(
                            lowestPrice,
                            sells,
                            levels
                    );

            return new SellSuggestion(
                    lowestPrice,
                    confidence,
                    "The market is sparse. "
                            + "The lowest active seller is currently the safest competitive position."
            );
        }

        /*
         * =====================================================
         * COMPETITIVE WINDOW
         * =====================================================
         *
         * A duplicate price only matters if it exists reasonably
         * close to the actual market floor.
         *
         * Example:
         *
         * 48
         * 49
         * 54
         * 59
         * 65
         * 99
         * 120
         * 120
         *
         * V1 saw:
         *
         * 120 x2
         *
         * and incorrectly suggested 120.
         *
         * V2 refuses to consider a supported cluster that far
         * away from the active market.
         */
        int competitiveWindow =
                calculateCompetitiveWindow(
                        lowestPrice
                );

        int maximumCompetitivePrice =
                lowestPrice
                        + competitiveWindow;

        /*
         * Find the FIRST supported price level inside
         * the believable competitive area.
         */
        int supportedIndex = -1;

        for (int i = 0; i < levels.size(); i++) {

            PriceLevel level =
                    levels.get(i);

            if (
                    level.price()
                            > maximumCompetitivePrice
            ) {
                break;
            }

            if (level.listings() >= 2) {

                supportedIndex = i;

                break;
            }
        }

        /*
         * =====================================================
         * CASE 3
         * NO SUPPORTED CLUSTER NEAR FLOOR
         * =====================================================
         *
         * Example:
         *
         * 48
         * 49
         * 54
         * 59
         * 65
         * 99
         * 120
         * 120
         *
         * There is no believable duplicated floor.
         *
         * However:
         *
         * 48 and 49 are close enough to form the immediate
         * competitive region.
         *
         * Suggest:
         *
         * 49
         *
         * rather than:
         *
         * 120
         */
        if (supportedIndex == -1) {

            int suggestedPrice =
                    lowestPrice;

            if (levels.size() >= 2) {

                int secondPrice =
                        levels.get(1)
                                .price();

                if (
                        secondPrice
                                - lowestPrice <= 2
                ) {

                    suggestedPrice =
                            secondPrice;
                }
            }

            int confidence =
                    calculateConfidence(
                            suggestedPrice,
                            sells,
                            levels
                    );

            return new SellSuggestion(
                    suggestedPrice,
                    confidence,
                    "No stable duplicate price exists near the market floor. "
                            + "The recommendation stays near the lowest active sellers."
            );
        }

        PriceLevel supportedLevel =
                levels.get(
                        supportedIndex
                );

        /*
         * =====================================================
         * CASE 4
         * DESCENDING PRICE STAIRCASE
         * =====================================================
         *
         * Example:
         *
         * 16
         * 17
         * 18
         * 19
         * 20
         * 20
         * 20
         * 20
         *
         * 20 is supported.
         *
         * BUT:
         *
         * There are already four sellers occupying every
         * price level between 16 and 19.
         *
         * This is different from:
         *
         * 15
         * 19
         * 20
         * 20
         * 20
         *
         * The first example represents a real downward trend.
         *
         * The second contains isolated undercuts.
         */
        if (
                supportedIndex >= 3
                        && isCompetitiveStaircase(
                        levels,
                        supportedIndex
                )
        ) {

            /*
             * Move approximately halfway into the staircase.
             *
             * Example:
             *
             * 16
             * 17
             * 18
             * 19
             * 20
             *
             * supportedIndex = 4
             *
             * targetIndex = 2
             *
             * Suggest:
             *
             * 18
             */
            int targetIndex =
                    supportedIndex / 2;

            int suggestedPrice =
                    levels.get(
                            targetIndex
                    ).price();

            int confidence =
                    calculateConfidence(
                            suggestedPrice,
                            sells,
                            levels
                    );

            return new SellSuggestion(
                    suggestedPrice,
                    confidence,
                    "Several consecutive sellers are already below the higher price cluster. "
                            + "The recommendation moves forward into the active downward price trend."
            );
        }

        /*
         * =====================================================
         * CASE 5
         * ISOLATED UNDERCUTS
         * =====================================================
         *
         * Example:
         *
         * 15
         * 19
         * 20
         * 20
         * 20
         * 20
         *
         * 15 is isolated.
         *
         * 19 is one seller.
         *
         * 20 has substantial support.
         *
         * Suggest:
         *
         * 20
         */
        int suggestedPrice =
                supportedLevel.price();

        int confidence =
                calculateConfidence(
                        suggestedPrice,
                        sells,
                        levels
                );

        return new SellSuggestion(
                suggestedPrice,
                confidence,
                "Lower listings appear isolated. "
                        + "The recommendation uses the first supported price inside the active market range."
        );
    }

    /*
     * Determines how far above the absolute market floor
     * we are willing to search for a supported price cluster.
     *
     * The window scales somewhat with item value,
     * but never becomes absurdly large.
     *
     * Examples:
     *
     * Lowest = 15
     * Window = 5
     *
     * Lowest = 20
     * Window = 5
     *
     * Lowest = 48
     * Window ≈ 10
     *
     * Lowest = 100
     * Window = 10
     */
    private int calculateCompetitiveWindow(
            int lowestPrice
    ) {

        int percentageWindow =
                (int) Math.round(
                        lowestPrice * 0.20
                );

        return Math.max(
                5,
                Math.min(
                        10,
                        percentageWindow
                )
        );
    }

    /*
     * Determines whether the prices below a supported cluster
     * form a believable downward staircase.
     *
     * Example:
     *
     * 16 -> 17 -> 18 -> 19 -> 20
     *
     * TRUE
     *
     *
     * Example:
     *
     * 15 -> 19 -> 20
     *
     * FALSE
     *
     * A gap larger than 2 platinum breaks the staircase.
     */
    private boolean isCompetitiveStaircase(
            List<PriceLevel> levels,
            int supportedIndex
    ) {

        for (int i = 1; i <= supportedIndex; i++) {

            int previous =
                    levels.get(i - 1)
                            .price();

            int current =
                    levels.get(i)
                            .price();

            if (
                    current
                            - previous > 2
            ) {

                return false;
            }
        }

        return true;
    }

    /*
     * =========================================================
     * CONFIDENCE
     * =========================================================
     *
     * Confidence answers:
     *
     * "How strongly does the currently visible in-game market
     * support THIS recommendation?"
     *
     * It does NOT mean:
     *
     * "Probability this item will sell."
     *
     *
     * Positive signals:
     *
     * - Multiple sellers at the suggested price.
     *
     *
     * Negative signals:
     *
     * - Many sellers already cheaper than us.
     * - Suggested price far above the active floor.
     * - Very sparse market.
     */
    private int calculateConfidence(
            int suggestedPrice,
            List<MarketOrderResponse> sells,
            List<PriceLevel> levels
    ) {

        int confidence = 80;

        int lowestPrice =
                levels.getFirst()
                        .price();

        long exactSupport =
                levels.stream()
                        .filter(level ->
                                level.price()
                                        == suggestedPrice
                        )
                        .mapToLong(
                                PriceLevel::listings
                        )
                        .findFirst()
                        .orElse(0);

        long sellersBelow =
                sells.stream()
                        .filter(order ->
                                order.platinum()
                                        < suggestedPrice
                        )
                        .count();

        int distanceFromFloor =
                suggestedPrice
                        - lowestPrice;

        /*
         * Multiple sellers agreeing on our price
         * increases confidence.
         */
        if (exactSupport >= 2) {
            confidence += 10;
        }

        if (exactSupport >= 4) {
            confidence += 10;
        }

        /*
         * Every seller already ahead of us matters.
         *
         * Maximum penalty:
         *
         * -30
         */
        confidence -=
                (int) Math.min(
                        30,
                        sellersBelow * 5
                );

        /*
         * Being far above the actual active floor
         * should heavily reduce confidence.
         *
         * Maximum penalty:
         *
         * -40
         */
        confidence -=
                Math.min(
                        40,
                        distanceFromFloor * 2
                );

        /*
         * Sparse markets contain less evidence.
         */
        if (sells.size() <= 5) {

            confidence -= 25;

        } else if (sells.size() <= 8) {

            confidence -= 10;
        }

        /*
         * Clamp:
         *
         * 0 <= confidence <= 100
         */
        return Math.max(
                0,
                Math.min(
                        100,
                        confidence
                )
        );
    }

    /*
     * Can be exposed to the frontend later.
     */
    private String confidenceLabel(
            int confidence
    ) {

        if (confidence >= 80) {
            return "HIGH";
        }

        if (confidence >= 55) {
            return "MEDIUM";
        }

        return "LOW";
    }

    /*
     * Represents one unique platinum price in
     * the active order book.
     */
    private record PriceLevel(
            int price,
            long listings
    ) {
    }

    /*
     * Represents the complete decision produced
     * by the pricing algorithm.
     *
     * This is intentionally more than just Integer.
     *
     * Price:
     *     What we recommend.
     *
     * Confidence:
     *     How strongly the current order book supports it.
     *
     * Reason:
     *     Why the algorithm chose it.
     */
    private record SellSuggestion(
            Integer price,
            int confidence,
            String reason
    ) {
    }
}