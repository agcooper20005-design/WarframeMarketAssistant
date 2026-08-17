package com.aces.warframepersonalextractor.dto;

public record OrderResponse(
        String id,
        String type,
        Integer platinum,
        Integer quantity,
        Integer rank,
        Boolean visible,
        String itemId,
        String itemName
) {
}