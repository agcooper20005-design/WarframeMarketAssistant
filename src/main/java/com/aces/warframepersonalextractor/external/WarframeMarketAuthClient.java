package com.aces.warframepersonalextractor.external;

import com.aces.warframepersonalextractor.dto.WarframeMarketLoginRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WarframeMarketAuthClient {


    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.warframe.market/v1")
            .defaultHeader("Language", "en")
            .defaultHeader("Platform", "pc")
            .defaultHeader("Crossplay", "true")
            .build();

    public String login(WarframeMarketLoginRequest request) {

        return restClient
                .post()
                .uri("/auth/signin")
                .header(HttpHeaders.AUTHORIZATION, "JWT")
                .body(new LoginBody(
                        "header",
                        request.email(),
                        request.password(),
                        request.deviceId()
                ))
                .exchange((req, response) -> {

                    String authorization =
                            response.getHeaders()
                                    .getFirst(HttpHeaders.AUTHORIZATION);

                    if (authorization == null) {
                        throw new IllegalStateException(
                                "Warframe.Market did not return an Authorization header"
                        );
                    }

                    if (!authorization.startsWith("JWT ")) {
                        throw new IllegalStateException(
                                "Unexpected Authorization header"
                        );
                    }

                    return authorization.substring(4);
                });
    }

    private record LoginBody(
            String auth_type,
            String email,
            String password,
            String device_id
    ) {
    }
}