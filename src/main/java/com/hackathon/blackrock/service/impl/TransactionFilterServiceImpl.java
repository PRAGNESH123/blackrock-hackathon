package com.hackathon.blackrock.service.impl;

import com.hackathon.blackrock.domain.*;
import com.hackathon.blackrock.model.request.FilterRequest;
import com.hackathon.blackrock.model.request.response.FilterResponse;
import com.hackathon.blackrock.service.TransactionFilterService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TransactionFilterServiceImpl implements TransactionFilterService {
    @Override
    public FilterResponse filter(FilterRequest request){
        if(request==null || request.getTransactions()==null || request.getTransactions().size()==0){
            return FilterResponse.builder()
                    .valid(List.of())
                    .invalid(List.of())
                    .build();
        }
        List<Transaction> validTransaction = new ArrayList<>();
        List<InvalidTransaction>  invalidTransaction = new ArrayList<>();

        Set<LocalDateTime> seenDates = new HashSet<>();
        for(Transaction transaction : request.getTransactions()){
            if(transaction.getAmount()<0){
                invalidTransaction.add(toInvalid(transaction, "Negative amounts are not allowed"));
                continue;
            }
            if(seenDates.contains(transaction.getDate())){
                invalidTransaction.add(toInvalid(transaction, "Date is already in use"));
                continue;
            }
            seenDates.add(transaction.getDate());
            double remanent = transaction.getRemanent();

            QPeriod matchingQ = findMatchingQPeriod(transaction.getDate(), request.getQ());
            if(matchingQ!=null){
                remanent = matchingQ.getFixed();
            }

            double totalExtra = sumOfMatchingPPeriods(transaction.getDate(), request.getP());
            remanent+=totalExtra;

            boolean inKPeriod = isInAnyKPeriod(transaction.getDate(), request.getK());

            Transaction txnEnriched = Transaction.builder()
                    .date(transaction.getDate())
                    .amount(transaction.getAmount())
                    .ceiling(transaction.getCeiling())
                    .remanent(remanent)
                    .inKPeriod(inKPeriod)
                    .build();

            validTransaction.add(txnEnriched);

        }
        return FilterResponse.builder()
                .valid(validTransaction)
                .invalid(invalidTransaction)
                .build();
    }

    private double sumOfMatchingPPeriods(LocalDateTime txnDate, List<PPeriod> pPeriods) {
        if (pPeriods == null || pPeriods.isEmpty()) return 0;

        return pPeriods.stream()
                .filter(p -> isWithinRange(txnDate, p.getStart(), p.getEnd()))
                .mapToDouble(PPeriod::getExtra)
                .sum();
    }

    private boolean isInAnyKPeriod(LocalDateTime txnDate, List<KPeriod> kPeriods) {
        if (kPeriods == null || kPeriods.isEmpty()) return true;
        return kPeriods.stream()
                .anyMatch(k -> isWithinRange(txnDate, k.getStart(), k.getEnd()));
    }

    private QPeriod findMatchingQPeriod(LocalDateTime txnDate, List<QPeriod> qPeriods) {
        if (qPeriods == null || qPeriods.isEmpty()) return null;

        QPeriod best        = null;
        int     bestIndex   = -1;

        for (int i = 0; i < qPeriods.size(); i++) {
            QPeriod q = qPeriods.get(i);

            if (isWithinRange(txnDate, q.getStart(), q.getEnd())) {

                if (best == null) {
                    best      = q;
                    bestIndex = i;

                } else {
                    int cmp = q.getStart().compareTo(best.getStart());

                    if (cmp > 0) {
                        best      = q;
                        bestIndex = i;

                    } else if (cmp == 0) {

                    }

                }
            }
        }

        return best;
    }

    private boolean isWithinRange(LocalDateTime date,
                                  LocalDateTime start,
                                  LocalDateTime end) {
        return !date.isBefore(start) && !date.isAfter(end);
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
}
