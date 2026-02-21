package com.hackathon.blackrock.service.impl;

import com.hackathon.blackrock.domain.Transaction;
import com.hackathon.blackrock.model.request.ExpenseRequest;
import com.hackathon.blackrock.model.request.response.TransactionResponse;
import com.hackathon.blackrock.service.TransactionParsingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionParserServiceImpl implements TransactionParsingService {

    @Override
    public TransactionResponse parser(List<ExpenseRequest> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            return TransactionResponse.builder()
                    .transactions(List.of())
                    .totalAmount(0)
                    .totalCeiling(0)
                    .totalRemanent(0)
                    .build();

        }
        List<Transaction> transactions = expenses.stream()
                .map(this::toTransaction)
                .toList();

        double totalAmount = transactions.stream().mapToDouble(Transaction::getAmount).sum();
        double totalCeiling = transactions.stream().mapToDouble(Transaction::getCeiling).sum();
        double totalRemanent = transactions.stream().mapToDouble(Transaction::getRemanent).sum();

        return TransactionResponse.builder()
                .transactions(transactions)
                .totalAmount(totalAmount)
                .totalCeiling(totalCeiling)
                .totalRemanent(totalRemanent)
                .build();
    }

    private Transaction toTransaction(ExpenseRequest expense) {
        double amount = expense.getAmount();
        double ceiling = computeCeiling(amount);
        double remanent = computeRemanent(amount, ceiling);

        return Transaction.builder()
                .date(expense.getDate())
                .amount(amount)
                .ceiling(ceiling)
                .remanent(remanent)
                .build();
    }

    private double computeCeiling(double amount) {
        if (amount <= 0) return 0;
        if (amount % 100 == 0) return amount;
        return (Math.floor(amount / 100) + 1) * 100;
    }

    private double computeRemanent(double amount, double ceiling) {
        if (amount <= 0) return 0;
        return ceiling - amount;
    }
}


