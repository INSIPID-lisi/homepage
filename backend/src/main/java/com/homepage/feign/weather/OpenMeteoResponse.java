package com.homepage.feign.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OpenMeteoResponse {

    @JsonProperty("current")
    private CurrentWeather current;

    @JsonProperty("daily")
    private DailyForecast daily;

    @Data
    public static class CurrentWeather {
        @JsonProperty("temperature_2m")
        private double temperature;

        @JsonProperty("relative_humidity_2m")
        private int humidity;

        @JsonProperty("weather_code")
        private int weatherCode;
    }

    @Data
    public static class DailyForecast {
        @JsonProperty("time")
        private List<String> time;

        @JsonProperty("temperature_2m_max")
        private List<Double> temperatureMax;

        @JsonProperty("temperature_2m_min")
        private List<Double> temperatureMin;

        @JsonProperty("weather_code")
        private List<Integer> weatherCode;
    }
}
