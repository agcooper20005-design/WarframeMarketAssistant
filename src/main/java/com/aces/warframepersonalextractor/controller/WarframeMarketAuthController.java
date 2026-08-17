package com.aces.warframepersonalextractor.controller;

import com.aces.warframepersonalextractor.dto.WarframeMarketLoginRequest;
import com.aces.warframepersonalextractor.external.WarframeMarketClient;
import com.aces.warframepersonalextractor.service.WarframeMarketAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class WarframeMarketAuthController {

    private final WarframeMarketAuthService authService;
    private final WarframeMarketClient warframeMarketClient;

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestBody WarframeMarketLoginRequest request
    ) {
        authService.login(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<String> me() {
        return ResponseEntity.ok(
                warframeMarketClient.getMe()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.ok().build();
    }
}