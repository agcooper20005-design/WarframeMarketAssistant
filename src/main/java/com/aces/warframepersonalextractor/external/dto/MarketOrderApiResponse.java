package com.aces.warframepersonalextractor.external.dto;

import java.util.List;

public record MarketOrderApiResponse(
        String apiVersion,
        List<MarketOrderResponse> data,
        Object error
) {
}