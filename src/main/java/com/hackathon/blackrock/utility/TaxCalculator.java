package com.hackathon.blackrock.utility;

public class TaxCalculator {
    public static double computeTax(double income) {
        if (income <= 700_000)   return 0;
        if (income <= 1_000_000) return (income - 700_000) * 0.10;

        double tax = 30_000;
        if (income <= 1_200_000) return tax + (income - 1_000_000) * 0.15;

        tax += 30_000;
        if (income <= 1_500_000) return tax + (income - 1_200_000) * 0.20;

        tax += 60_000;
        return tax + (income - 1_500_000) * 0.30;
    }


    public static double computeTaxBenefit(double invested, double annualIncome) {
        double npsDeduction = Math.min(invested,
                Math.min(annualIncome * 0.10, 200_000));

        return computeTax(annualIncome) - computeTax(annualIncome - npsDeduction);
    }
}
