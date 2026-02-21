package com.hackathon.blackrock.controller;

import com.hackathon.blackrock.model.request.response.PerformanceResponse;
import com.hackathon.blackrock.service.PerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blackrock/challenge/v1")
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    @GetMapping("/performance")
    public ResponseEntity<PerformanceResponse> getPerformance() {
        return ResponseEntity.ok(performanceService.getMetrics());
    }}
