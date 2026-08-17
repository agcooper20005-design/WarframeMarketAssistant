package com.aces.warframepersonalextractor.external.dto;

import java.util.Map;
import java.util.Set;

public record MarketItemResponse(
        String id,
        String slug,
        String gameRef,
        Set<String> tags,
        Integer maxRank,
        Boolean tradable,
        Boolean bulkTradable,
        String rarity,
        Map<String, ItemTranslationResponse> i18n
) {
}