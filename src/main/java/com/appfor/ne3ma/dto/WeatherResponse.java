package com.appfor.ne3ma.dto;

public record WeatherResponse(
        double latitude,
        double longitude,
        CurrentWeatherData current,
        TodayWeatherData today
) {
}
