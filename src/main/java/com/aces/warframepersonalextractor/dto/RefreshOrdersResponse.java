package com.aces.warframepersonalextractor.dto;

import java.util.List;

public record RefreshOrdersResponse(
        Integer ordersChecked,
        Integer ordersChanged,
        Integer ordersUnchanged,
        Integer ordersSkipped,
        List<OrderRefreshResult> results
) {
}