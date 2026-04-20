package com.appfor.ne3ma.service;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


@Service
public class WeatherService {

    private final WebClient webClient;

    public WeatherService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.open-meteo.com/v1")
                .build();
    }

    public String getWeather(double lat, double lon) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParam("latitude", lat)
                        .queryParam("longitude", lon)
                        .queryParam("daily", "temperature_2m_max,temperature_2m_min")
                        .queryParam("hourly", "relative_humidity_2m")
                        .queryParam("current_weather", true)
                        .queryParam("forecast_days", 1)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}