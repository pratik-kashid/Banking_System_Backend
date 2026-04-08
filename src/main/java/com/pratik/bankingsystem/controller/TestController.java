package com.pratik.bankingsystem.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String home() {
        return "🚀 Banking Backend is LIVE!";
    }

    @GetMapping("/api/test")
    public String test() {
        return "API Working ✅";
    }
}