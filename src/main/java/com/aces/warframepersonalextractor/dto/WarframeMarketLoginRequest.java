package com.aces.warframepersonalextractor.dto;

public record WarframeMarketLoginRequest(
        String email,
        String password,
        String deviceId
) {
}