package com.hackathon.blackrock.controller;

import com.hackathon.blackrock.model.request.ExpenseRequest;
import com.hackathon.blackrock.model.request.FilterRequest;
import com.hackathon.blackrock.model.request.ValidatorRequest;
import com.hackathon.blackrock.model.request.response.FilterResponse;
import com.hackathon.blackrock.model.request.response.TransactionResponse;
import com.hackathon.blackrock.model.request.response.ValidatorResponse;
import com.hackathon.blackrock.service.TransactionFilterService;
import com.hackathon.blackrock.service.TransactionParsingService;
import com.hackathon.blackrock.service.TransactionValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/blackrock/challenge/v1")
public class TransactionController {

    @Autowired private TransactionParsingService parserService;
    @Autowired private TransactionValidatorService validatorService;
    @Autowired
    private TransactionFilterService filterService;

    @PostMapping("/transactions:parse")
    public ResponseEntity<TransactionResponse> parse(
            @RequestBody List<ExpenseRequest> expenses) {
        return ResponseEntity.ok(parserService.parser(expenses));
    }

    @PostMapping("/transactions:validator")
    public ResponseEntity<ValidatorResponse> validate(
            @RequestBody ValidatorRequest request) {
        return ResponseEntity.ok(validatorService.validate(request));
    }

    @PostMapping("/transactions:filter")
    public ResponseEntity<FilterResponse> filter(
            @RequestBody FilterRequest request) {
        return ResponseEntity.ok(filterService.filter(request));
    }
}
