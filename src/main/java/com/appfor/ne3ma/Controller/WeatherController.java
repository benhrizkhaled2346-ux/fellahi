package com.appfor.ne3ma.Controller;

import com.appfor.ne3ma.dto.WeatherRequest;
import com.appfor.ne3ma.service.WeatherService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @PostMapping
    public String getWeather(@RequestBody WeatherRequest weath) {
        return weatherService.getWeather(weath.getLat(), weath.getLon());
    }
}
