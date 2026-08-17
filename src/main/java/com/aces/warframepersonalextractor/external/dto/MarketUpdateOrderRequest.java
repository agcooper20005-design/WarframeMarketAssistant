package com.aces.warframepersonalextractor.external.dto;

public record MarketUpdateOrderRequest(
        Integer platinum,
        Integer quantity,
        Boolean visible,
        Integer perTrade,
        Integer rank
) {
}