package com.aces.warframepersonalextractor.dto;

public record OrderAnalysisResponse(
        String orderId,
        String itemName,
        Integer currentPrice,
        Integer lowestSell,
        Integer highestBuy,
        Integer suggestedSell,
        String reason
) {
}