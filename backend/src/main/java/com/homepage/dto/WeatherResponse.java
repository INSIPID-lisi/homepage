package com.homepage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {
    private CurrentWeather current;
    private List<DailyForecast> daily;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentWeather {
        private double temperature;
        private int humidity;
        private int weatherCode;
        private String weatherDesc;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyForecast {
        private String date;
        private double tempMax;
        private double tempMin;
        private int weatherCode;
        private String weatherDesc;
    }
}
