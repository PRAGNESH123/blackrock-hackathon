package com.hackathon.blackrock.service.impl;

import com.hackathon.blackrock.domain.InvalidTransaction;
import com.hackathon.blackrock.domain.Transaction;
import com.hackathon.blackrock.model.request.ValidatorRequest;
import com.hackathon.blackrock.model.request.response.ValidatorResponse;
import com.hackathon.blackrock.service.TransactionValidatorService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TransactionValidatorServiceImpl implements TransactionValidatorService {

    private static final double MAX_AMOUNT =  500_000;

    @Override
    public ValidatorResponse validate(ValidatorRequest request){
        if(request==null || request.getTransactions()==null){
            return ValidatorResponse.builder()
                    .valid(List.of())
                    .invalid(List.of())
                    .build();
        }
        List<Transaction> validTransaction = new ArrayList<>();
        List<InvalidTransaction> invalidTransactions = new ArrayList<>();

        Set<LocalDateTime> seenDates = new HashSet<>();

        for(Transaction t: request.getTransactions()){
            if(t.getAmount()<0){
                invalidTransactions.add(toInvalid(t, "No Negative amounts are allowed"));
                continue;
            }
            if(t.getAmount()>=MAX_AMOUNT){
                invalidTransactions.add(toInvalid(t, "Amount too large for transaction"));
                continue;
            }
            if(seenDates.contains(t.getDate())){
                invalidTransactions.add(toInvalid(t, "Transaction has already been seen"));
                continue;
            }
            if(!isCeilingValid(t)){
                invalidTransactions.add(toInvalid(t, "Ceiling is not valid"));
                continue;
            }
            seenDates.add(t.getDate());
            validTransaction.add(t);
        }
        return ValidatorResponse.builder()
                .valid(validTransaction)
                .invalid(invalidTransactions)
                .build();
    }

    private InvalidTransaction toInvalid(Transaction txn, String message) {
        return InvalidTransaction.builder()
                .date(txn.getDate())
                .amount(txn.getAmount())
                .ceiling(txn.getCeiling())
                .remanent(txn.getRemanent())
                .message(message)
                .build();
    }

    private boolean isCeilingValid(Transaction txn) {
        double amount  = txn.getAmount();
        double ceiling = txn.getCeiling();
        double remanent = txn.getRemanent();

        double expectedCeiling;
        if (amount <= 0) {
            expectedCeiling = 0;
        } else if (amount % 100 == 0) {
            expectedCeiling = amount;
        } else {
            expectedCeiling = (Math.floor(amount / 100) + 1) * 100;
        }

        double expectedRemanent = expectedCeiling - amount;

        boolean ceilingMatch  = Math.abs(ceiling - expectedCeiling)   < 0.001;
        boolean remanentMatch = Math.abs(remanent - expectedRemanent) < 0.001;

        return ceilingMatch && remanentMatch;
    }
}
