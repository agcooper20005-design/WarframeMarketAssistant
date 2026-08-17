package com.aces.warframepersonalextractor.external.dto;

public record ItemTranslationResponse(
        String name,
        String description,
        String wikiLink,
        String icon,
        String thumb
) {
}