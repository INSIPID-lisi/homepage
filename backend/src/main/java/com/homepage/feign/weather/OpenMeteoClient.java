package com.homepage.feign.weather;

import com.homepage.feign.weather.config.OpenMeteoFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "openMeteoClient",
        url = "https://api.open-meteo.com",
        configuration = OpenMeteoFeignConfig.class
)
public interface OpenMeteoClient {

    @GetMapping("/v1/forecast")
    OpenMeteoResponse getForecast(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("current") String currentParams,
            @RequestParam("daily") String dailyParams,
            @RequestParam("timezone") String timezone
    );
}
