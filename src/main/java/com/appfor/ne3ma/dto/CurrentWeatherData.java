package com.appfor.ne3ma.dto;

public record CurrentWeatherData(
        double temperature,
        double humidity,
        double windSpeed,
        int weatherCode,
        int isDay
) {
}
