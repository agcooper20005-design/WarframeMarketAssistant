package com.aces.warframepersonalextractor.dto;

public record OrderPriceChangeResponse(
        String orderId,
        String itemName,
        Integer oldPrice,
        Integer newPrice,
        String reason
) {
}