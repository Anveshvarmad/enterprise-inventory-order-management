package com.enterprise.inventory.service;

import com.enterprise.inventory.dto.DemandForecastResponse;
import com.enterprise.inventory.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class MachineLearningService {

    private final RestTemplate restTemplate;

    @Value("${ml.service.url:${ML_SERVICE_URL:http://localhost:8090}}")
    private String mlServiceUrl;

    public MachineLearningService() {
        this.restTemplate = new RestTemplate();
    }

    public DemandForecastResponse getDemandForecast() {
        try {
            return restTemplate.getForObject(
                    mlServiceUrl + "/forecast",
                    DemandForecastResponse.class
            );
        } catch (RestClientException exception) {
            throw new BusinessRuleException("ML demand forecasting service is unavailable");
        }
    }
}
