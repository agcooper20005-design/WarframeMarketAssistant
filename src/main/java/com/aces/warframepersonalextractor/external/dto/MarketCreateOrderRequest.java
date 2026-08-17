package com.aces.warframepersonalextractor.external.dto;

public record MarketCreateOrderRequest(
        String itemId,
        String type,
        Integer platinum,
        Integer quantity,
        Boolean visible,

        Integer perTrade,
        Integer rank,
        Integer charges,
        String subtype,
        Integer amberStars,
        Integer cyanStars
) {
}