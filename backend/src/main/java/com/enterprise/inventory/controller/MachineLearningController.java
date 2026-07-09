package com.enterprise.inventory.controller;

import com.enterprise.inventory.dto.DemandForecastResponse;
import com.enterprise.inventory.service.MachineLearningService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MachineLearningController {

    private final MachineLearningService machineLearningService;

    public MachineLearningController(MachineLearningService machineLearningService) {
        this.machineLearningService = machineLearningService;
    }

    @GetMapping("/api/ml/demand-forecast")
    public DemandForecastResponse getDemandForecast() {
        return machineLearningService.getDemandForecast();
    }
}
