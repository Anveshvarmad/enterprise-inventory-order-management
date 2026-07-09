package com.enterprise.inventory.dto;

import java.util.List;

public record DemandForecastResponse(
        String generatedAt,
        String modelName,
        String modelVersion,
        Integer totalItemsScored,
        Integer highRiskItems,
        Integer mediumRiskItems,
        Integer lowRiskItems,
        List<DemandForecastItemResponse> forecasts
) {
}
