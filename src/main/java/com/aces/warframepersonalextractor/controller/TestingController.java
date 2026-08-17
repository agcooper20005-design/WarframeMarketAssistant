package com.aces.warframepersonalextractor.controller;


import com.aces.warframepersonalextractor.external.WarframeMarketClient;
import com.aces.warframepersonalextractor.external.dto.ItemApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/testing")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TestingController {

    private final WarframeMarketClient warframeMarketClient;

    @GetMapping("/items")
    public ItemApiResponse getAllItems() {
        System.out.println("ouch");
        return warframeMarketClient.getAllItems();
    }
    @GetMapping("/hello")
    public String hello() {
        return "Hello World!, I am Ace";
    }

}
