package com.appfor.ne3ma.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/")
public class HomeController {
    @GetMapping
    public ResponseEntity<String> Home(){
        return ResponseEntity.ok("building time");
    }
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
