package com.aces.warframepersonalextractor.external.dto.market;

public record MarketListingSnapshot(
        String orderId,
        String seller,
        String type,
        Integer platinum,
        Integer quantity,
        Integer rank
) {
}