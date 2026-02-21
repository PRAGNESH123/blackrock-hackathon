package com.hackathon.blackrock.model.request;

import com.hackathon.blackrock.domain.Transaction;
import lombok.Data;

import java.util.List;

@Data
public class ValidatorRequest {
    private double wage;
    private List<Transaction> transactions;
}
