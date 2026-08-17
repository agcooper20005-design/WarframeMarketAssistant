package com.aces.warframepersonalextractor.external.dto;

import tools.jackson.databind.JsonNode;

import java.util.List;

public record ItemApiResponse(
        String apiVersion,
        List<MarketItemResponse> data,
        JsonNode error
) {
}
