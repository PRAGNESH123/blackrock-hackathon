package com.hackathon.blackrock.service.impl;

import com.hackathon.blackrock.domain.*;
import com.hackathon.blackrock.model.request.ReturnsRequest;
import com.hackathon.blackrock.model.request.response.ReturnsResponse;
import com.hackathon.blackrock.service.ReturnsCalculationService;
import com.hackathon.blackrock.utility.TaxCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReturnsCalculationServiceImpl implements ReturnsCalculationService {
    private static final double NPS_RATE = 0.0711;
    private static final double IDX_RATE = 0.1449;

    private static final int COMPOUND_FREQ = 1;

    private static final int RETIREMENT_AGE = 60;
    private static final int MIN_INVESTMENT_YEARS = 5;

    @Override
    public ReturnsResponse calculate(ReturnsRequest request, boolean isNPS){
        if(request==null || request.getTransactions()==null){
            return ReturnsResponse.builder()
                    .totalTransactionAmount(0)
                    .totalCeiling(0)
                    .savingsByDates(List.of())
                    .build();
        }

        List<Transaction> validTransactions = filterValidTransactions(request.getTransactions());

        List<Transaction> enrichedTransactions = applyPeriodRules(validTransactions, request.getQ(), request.getP());



        double totalAmount = enrichedTransactions.stream().mapToDouble(Transaction::getAmount).sum();
        double totalCeiling = enrichedTransactions.stream().mapToDouble(Transaction::getCeiling).sum();

        double annualIncome = request.getWage() * 12;
        int    years   = computeInvestmentYears(request.getAge());
        double rate    = isNPS ? NPS_RATE : IDX_RATE;

        List<KPeriodResult> saveByDates = new ArrayList<>();

        for(KPeriod kPeriod : request.getK()){
            double investmentAmount = enrichedTransactions.stream()
                    .filter(transaction -> isWithinRange(transaction.getDate(), kPeriod.getStart(), kPeriod.getEnd()))
                    .mapToDouble(Transaction::getRemanent)
                    .sum();

            double finalAmount = compoundInterest(investmentAmount, rate, years);

            double realAmount  = inflationAdjust(finalAmount, request.getInflation(), years);

            double profit = realAmount - finalAmount;

            double taxBenefit  = 0;
            if (isNPS) {
                taxBenefit = TaxCalculator.computeTaxBenefit(investmentAmount, annualIncome);
            }

            saveByDates.add(KPeriodResult.builder()
                    .start(kPeriod.getStart())
                    .end(kPeriod.getEnd())
                    .amount(round(investmentAmount))
                    .profit(round(profit))
                    .taxBenefit(round(taxBenefit))
                    .build());



        }
        return ReturnsResponse.builder()
                .totalTransactionAmount(round(totalAmount))
                .totalCeiling(round(totalCeiling))
                .savingsByDates(saveByDates)
                .build();



    }

    private List<Transaction> filterValidTransactions(List<Transaction> transactions) {
        List<Transaction> valid       = new ArrayList<>();
        Set<LocalDateTime> seenDates  = new HashSet<>();

        for (Transaction txn : transactions) {
            if (txn.getAmount() < 0)                  continue; // negative
            if (seenDates.contains(txn.getDate()))     continue; // duplicate
            seenDates.add(txn.getDate());
            valid.add(txn);
        }
        return valid;
    }

    private List<Transaction> applyPeriodRules(List<Transaction> transactions,
                                               List<QPeriod> qPeriods,
                                               List<PPeriod> pPeriods) {
        List<Transaction> result = new ArrayList<>();

        for (Transaction txn : transactions) {
            double remanent = txn.getRemanent();

            // Q: replace remanent if transaction falls in a q period
            QPeriod matchedQ = findMatchingQPeriod(txn.getDate(), qPeriods);
            if (matchedQ != null) {
                remanent = matchedQ.getFixed();
            }

            // P: add ALL matching p period extras on top
            double totalExtra = sumMatchingPPeriods(txn.getDate(), pPeriods);
            remanent += totalExtra;

            // Build new transaction with updated remanent — immutable approach
            result.add(Transaction.builder()
                    .date(txn.getDate())
                    .amount(txn.getAmount())
                    .ceiling(txn.getCeiling())
                    .remanent(remanent)
                    .build());
        }
        return result;
    }

    private QPeriod findMatchingQPeriod(LocalDateTime date, List<QPeriod> qPeriods) {
        if (qPeriods == null || qPeriods.isEmpty()) return null;

        QPeriod best = null;

        for (QPeriod q : qPeriods) {
            if (!isWithinRange(date, q.getStart(), q.getEnd())) continue;

            if (best == null) {
                best = q;
            } else {
                int cmp = q.getStart().compareTo(best.getStart());
                if (cmp > 0) best = q; // later start wins
                // cmp == 0 → tie → keep first (best already set)
                // cmp < 0  → earlier start → skip
            }
        }
        return best;
    }

    private double sumMatchingPPeriods(LocalDateTime date, List<PPeriod> pPeriods) {
        if (pPeriods == null || pPeriods.isEmpty()) return 0;

        return pPeriods.stream()
                .filter(p -> isWithinRange(date, p.getStart(), p.getEnd()))
                .mapToDouble(PPeriod::getExtra)
                .sum();
    }

    private double compoundInterest(double principal, double rate, int years) {
        return principal * Math.pow(1 + rate / COMPOUND_FREQ,
                (double) COMPOUND_FREQ * years);
    }

    private double inflationAdjust(double amount, double inflation, int years) {
        return amount / Math.pow(1 + inflation, years);
    }

    private int computeInvestmentYears(int age) {
        return (age >= RETIREMENT_AGE)
                ? MIN_INVESTMENT_YEARS
                : RETIREMENT_AGE - age;
    }


    private boolean isWithinRange(LocalDateTime date,
                                  LocalDateTime start,
                                  LocalDateTime end) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

}
