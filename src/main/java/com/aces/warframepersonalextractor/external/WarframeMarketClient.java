package com.aces.warframepersonalextractor.external;

import com.aces.warframepersonalextractor.external.dto.ItemApiResponse;
import com.aces.warframepersonalextractor.external.dto.MarketCreateOrderRequest;
import com.aces.warframepersonalextractor.external.dto.MarketOrderApiResponse;
import com.aces.warframepersonalextractor.external.dto.MarketOrdersApiResponse;
import com.aces.warframepersonalextractor.external.dto.MarketUpdateOrderRequest;
import com.aces.warframepersonalextractor.service.WarframeMarketAuthService;
import com.aces.warframepersonalextractor.service.WarframeMarketRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class WarframeMarketClient {

    private final WarframeMarketAuthService authService;
    private final WarframeMarketRateLimiter rateLimiter;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.warframe.market/v2")
            .defaultHeader("Language", "en")
            .defaultHeader("Platform", "pc")
            .defaultHeader("Crossplay", "true")
            .build();

    public ItemApiResponse getAllItems() {
        rateLimiter.waitForRequestSlot();
        return restClient
                .get()
                .uri("/items")
                .retrieve()
                .body(ItemApiResponse.class);
    }

    public MarketOrderApiResponse getMyOrders() {
        rateLimiter.waitForRequestSlot();
        RestClient.RequestHeadersSpec<?> request = restClient
                .get()
                .uri("/orders/my");

        addAuthorizationHeader(request);

        return request
                .retrieve()
                .body(MarketOrderApiResponse.class);
    }

    public String getMe() {
        rateLimiter.waitForRequestSlot();
        RestClient.RequestHeadersSpec<?> request = restClient
                .get()
                .uri("/me");

        addAuthorizationHeader(request);

        return request
                .retrieve()
                .body(String.class);
    }

    public String createOrder(
            MarketCreateOrderRequest body
    ) {

        rateLimiter.waitForRequestSlot();

        RestClient.RequestBodySpec request = restClient
                .post()
                .uri("/order");

        request.header(
                "Authorization",
                "Bearer " + authService.getJwtToken()
        );

        return request
                .body(body)
                .retrieve()
                .body(String.class);
    }

    public void deleteOrder(
            String orderId
    ) {
        rateLimiter.waitForRequestSlot();

        RestClient.RequestHeadersSpec<?> request = restClient
                .delete()
                .uri(
                        "/order/{id}",
                        orderId
                );

        addAuthorizationHeader(request);

        request
                .retrieve()
                .toBodilessEntity();
    }

    public String updateOrder(
            String orderId,
            MarketUpdateOrderRequest body
    ) {

        rateLimiter.waitForRequestSlot();

        RestClient.RequestBodySpec request = restClient
                .patch()
                .uri(
                        "/order/{id}",
                        orderId
                );

        request.header(
                "Authorization",
                "Bearer " + authService.getJwtToken()
        );

        return request
                .body(body)
                .retrieve()
                .body(String.class);
    }

    public MarketOrdersApiResponse getOrdersForItem(
            String marketItemId
    ) {
        rateLimiter.waitForRequestSlot();

        return restClient
                .get()
                .uri(
                        "/orders/itemId/{itemId}",
                        marketItemId
                )
                .retrieve()
                .body(MarketOrdersApiResponse.class);
    }

    private void addAuthorizationHeader(
            RestClient.RequestHeadersSpec<?> request
    ) {
        rateLimiter.waitForRequestSlot();

        request.header(
                "Authorization",
                "Bearer " + authService.getJwtToken()
        );
    }
}