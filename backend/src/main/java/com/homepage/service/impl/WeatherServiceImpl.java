package com.homepage.service.impl;

import com.homepage.dto.WeatherResponse;
import com.homepage.feign.weather.OpenMeteoClient;
import com.homepage.feign.weather.OpenMeteoResponse;
import com.homepage.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final OpenMeteoClient openMeteoClient;

    @Value("${app.weather.latitude}")
    private double latitude;

    @Value("${app.weather.longitude}")
    private double longitude;

    @Value("${app.weather.cache-minutes:60}")
    private long cacheMinutes;

    private WeatherResponse cachedResponse;
    private Instant lastFetch = Instant.EPOCH;

    @Override
    public synchronized WeatherResponse getWeather() {
        if (cachedResponse != null
                && Duration.between(lastFetch, Instant.now()).toMinutes() < cacheMinutes) {
            return cachedResponse;
        }
        return fetchWeather();
    }

    private WeatherResponse fetchWeather() {
        OpenMeteoResponse raw = openMeteoClient.getForecast(
                latitude, longitude,
                "temperature_2m,relative_humidity_2m,weather_code",
                "temperature_2m_max,temperature_2m_min,weather_code",
                "auto"
        );

        WeatherResponse resp = new WeatherResponse();
        if (raw.getCurrent() != null) {
            resp.setCurrent(new WeatherResponse.CurrentWeather(
                    raw.getCurrent().getTemperature(),
                    raw.getCurrent().getHumidity(),
                    raw.getCurrent().getWeatherCode(),
                    decodeWeatherCode(raw.getCurrent().getWeatherCode())
            ));
        }

        if (raw.getDaily() != null && raw.getDaily().getTime() != null) {
            List<WeatherResponse.DailyForecast> dailyList = new ArrayList<>();
            for (int i = 0; i < raw.getDaily().getTime().size(); i++) {
                int code = raw.getDaily().getWeatherCode().get(i);
                dailyList.add(new WeatherResponse.DailyForecast(
                        raw.getDaily().getTime().get(i),
                        raw.getDaily().getTemperatureMax().get(i),
                        raw.getDaily().getTemperatureMin().get(i),
                        code,
                        decodeWeatherCode(code)
                ));
            }
            resp.setDaily(dailyList);
        }

        cachedResponse = resp;
        lastFetch = Instant.now();
        return resp;
    }

    private String decodeWeatherCode(int code) {
        if (code == 0) return "晴天";
        if (code == 1) return "大部晴朗";
        if (code == 2) return "多云";
        if (code == 3) return "阴天";
        if (code == 45 || code == 48) return "雾";
        if (code >= 51 && code <= 57) return "毛毛雨";
        if (code >= 61 && code <= 65) return "雨";
        if (code == 66 || code == 67) return "冻雨";
        if (code >= 71 && code <= 77) return "雪";
        if (code >= 80 && code <= 82) return "阵雨";
        if (code >= 85 && code <= 86) return "阵雪";
        if (code >= 95) return "雷暴";
        return "未知";
    }
}
