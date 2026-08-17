package com.aces.warframepersonalextractor.controller;

import com.aces.warframepersonalextractor.dto.*;
import com.aces.warframepersonalextractor.external.dto.UpdateOrderRequest;
import com.aces.warframepersonalextractor.service.ItemService;
import com.aces.warframepersonalextractor.service.WarframeMarketOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class WarframeMarketOrderController {

    private final WarframeMarketOrderService orderService;
    private final ItemService itemService;
    private final WarframeMarketOrderService warframeMarketOrderService;


    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(itemService.createOrder(request));

    }
    @PostMapping("/{orderId}/analyze")
    public ResponseEntity<OrderAnalysisResponse> analyzeOrder(
            @PathVariable String orderId
    ) {

        return ResponseEntity.ok(
                warframeMarketOrderService
                        .analyzeOrder(orderId)
        );
    }
    @PostMapping("/{orderId}/apply-suggested")
    public ResponseEntity<OrderPriceChangeResponse> applySuggested(
            @PathVariable String orderId
    ) {

        return ResponseEntity.ok(
                warframeMarketOrderService
                        .applySuggestedPrice(orderId)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshOrdersResponse>
    refreshAllOrders() {

        return ResponseEntity.ok(
                warframeMarketOrderService
                        .refreshAllOrders()
        );
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<String> updateOrder(
            @PathVariable String orderId,
            @RequestBody UpdateOrderRequest request
    ) {

        return ResponseEntity.ok(
                warframeMarketOrderService.updateOrder(
                        orderId,
                        request
                )
        );
    }


    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable String orderId
    ) {

        warframeMarketOrderService.deleteOrder(
                orderId
        );

        return ResponseEntity.noContent().build();
    }


    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders() {

        return ResponseEntity.ok(
                orderService.getMyOrders()
        );
    }

}