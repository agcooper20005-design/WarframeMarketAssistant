package com.aces.warframepersonalextractor.service;

import com.aces.warframepersonalextractor.dto.WarframeMarketLoginRequest;
import com.aces.warframepersonalextractor.external.WarframeMarketAuthClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WarframeMarketAuthService {

    private final WarframeMarketAuthClient warframeMarketAuthClient;

    @Getter
    private String jwtToken;

    public void login(WarframeMarketLoginRequest request) {

        jwtToken = warframeMarketAuthClient.login(request);
    }

    public void logout() {

        jwtToken = null;
    }

    public boolean isAuthenticated() {

        return jwtToken != null && !jwtToken.isBlank();
    }

}