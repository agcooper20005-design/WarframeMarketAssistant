package com.aces.warframepersonalextractor.external.dto;

import java.time.Instant;

public record MarketOrderResponse(
        String id,
        String type,
        Integer platinum,
        Integer quantity,
        Integer perTrade,
        Integer rank,
        Boolean visible,
        Instant createdAt,
        Instant updatedAt,
        String itemId,
        MarketOrderUserResponse user
) {
}