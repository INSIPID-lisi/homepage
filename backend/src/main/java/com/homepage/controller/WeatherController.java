package com.homepage.controller;

import com.homepage.dto.Result;
import com.homepage.dto.WeatherResponse;
import com.homepage.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/weather")
    public Result<WeatherResponse> getWeather() {
        return Result.success(weatherService.getWeather());
    }
}
