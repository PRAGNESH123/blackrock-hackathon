package com.hackathon.blackrock.model.request.response;

import com.hackathon.blackrock.domain.Transaction;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TransactionResponse {
    private List<Transaction> transactions;
    private double totalAmount;
    private double totalCeiling;
    private double totalRemanent;
}
