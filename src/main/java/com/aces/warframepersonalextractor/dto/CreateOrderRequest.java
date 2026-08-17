package com.aces.warframepersonalextractor.dto;

public record CreateOrderRequest(
        String marketItemId,
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