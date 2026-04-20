package com.appfor.ne3ma.Controller;

import com.appfor.ne3ma.service.WeatherService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public String getWeather(@RequestBody double lat,
                             @RequestBody double lon) {
        return weatherService.getWeather(lat, lon);
    }
}
