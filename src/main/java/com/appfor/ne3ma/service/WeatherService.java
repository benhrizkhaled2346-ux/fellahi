package com.appfor.ne3ma.service;

import com.appfor.ne3ma.dto.CurrentWeatherData;
import com.appfor.ne3ma.dto.TodayWeatherData;
import com.appfor.ne3ma.dto.WeatherResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class WeatherService {

    private final WebClient webClient;

    public WeatherService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.open-meteo.com/v1")
                .build();
    }

    public WeatherResponse getWeather(double lat, double lon) {
        Map<String, Object> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParam("latitude", lat)
                        .queryParam("longitude", lon)
                        .queryParam("current", "temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code,is_day")
                        .queryParam("daily", "temperature_2m_max,temperature_2m_min")
                        .queryParam("forecast_days", 1)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) {
            throw new IllegalStateException("Weather data is unavailable");
        }

        if (!(response.get("current") instanceof Map<?, ?> current)) {
            throw new IllegalStateException("Current weather data is unavailable");
        }

        if (!(response.get("daily") instanceof Map<?, ?> daily)) {
            throw new IllegalStateException("Daily weather data is unavailable");
        }

        CurrentWeatherData currentWeather = new CurrentWeatherData(
                getDouble(current, "temperature_2m"),
                getDouble(current, "relative_humidity_2m"),
                getDouble(current, "wind_speed_10m"),
                getInt(current, "weather_code"),
                getInt(current, "is_day")
        );

        TodayWeatherData todayWeather = new TodayWeatherData(
                getFirstDouble(daily, "temperature_2m_max"),
                getFirstDouble(daily, "temperature_2m_min")
        );

        return new WeatherResponse(lat, lon, currentWeather, todayWeather);
    }

    private double getDouble(Map<?, ?> source, String key) {
        Object value = source.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        throw new IllegalStateException("Missing numeric weather field: " + key);
    }

    private int getInt(Map<?, ?> source, String key) {
        Object value = source.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new IllegalStateException("Missing integer weather field: " + key);
    }

    private double getFirstDouble(Map<?, ?> source, String key) {
        Object value = source.get(key);
        if (value instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Number number) {
            return number.doubleValue();
        }
        throw new IllegalStateException("Missing daily weather field: " + key);
    }
}
