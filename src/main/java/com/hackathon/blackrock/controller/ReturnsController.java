package com.hackathon.blackrock.controller;

import com.hackathon.blackrock.model.request.ReturnsRequest;
import com.hackathon.blackrock.model.request.response.ReturnsResponse;
import com.hackathon.blackrock.service.ReturnsCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blackrock/challenge/v1")
public class ReturnsController {

    @Autowired
    private ReturnsCalculationService returnsService;

    @PostMapping("/returns:nps")
    public ResponseEntity<ReturnsResponse> nps(@RequestBody ReturnsRequest request) {
        return ResponseEntity.ok(returnsService.calculate(request, true));
    }

    @PostMapping("/returns:index")
    public ResponseEntity<ReturnsResponse> index(@RequestBody ReturnsRequest request) {
        return ResponseEntity.ok(returnsService.calculate(request, false));
    }
}
