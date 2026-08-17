package com.aces.warframepersonalextractor.controller;

import com.aces.warframepersonalextractor.external.dto.market.MarketAnalysisResponse;
import com.aces.warframepersonalextractor.service.MarketAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketAnalysisController {

    private final MarketAnalysisService marketAnalysisService;

    @GetMapping("/{marketItemId}/analysis")
    public ResponseEntity<MarketAnalysisResponse> analyzeItem(@PathVariable String marketItemId) {

        return ResponseEntity.ok(
                marketAnalysisService.analyzeItem(
                        marketItemId
                )
        );
    }
}