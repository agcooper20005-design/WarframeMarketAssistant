package com.aces.warframepersonalextractor.dto;

public record OrderRefreshResult(
        String orderId,
        String itemName,
        Integer oldPrice,
        Integer suggestedPrice,
        Boolean changed,
        String message
) {
}