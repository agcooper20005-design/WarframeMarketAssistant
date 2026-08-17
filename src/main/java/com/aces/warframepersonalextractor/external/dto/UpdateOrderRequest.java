package com.aces.warframepersonalextractor.external.dto;

public record UpdateOrderRequest(
        Integer platinum,
        Integer quantity,
        Boolean visible,
        Integer perTrade,
        Integer rank
) {
}