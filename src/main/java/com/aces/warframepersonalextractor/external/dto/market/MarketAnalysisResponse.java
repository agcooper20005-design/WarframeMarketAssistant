package com.aces.warframepersonalextractor.external.dto.market;

import java.util.List;

public record MarketAnalysisResponse(
        String marketItemId,
        String itemName,

        Integer lowestSell,
        Integer highestBuy,
        Integer suggestedSell,

        Integer confidence,
        String confidenceLabel,

        Integer inGameSellCount,
        Integer inGameBuyCount,

        List<MarketListingSnapshot> sellListings,
        List<MarketListingSnapshot> buyListings,

        String reason
) {
}