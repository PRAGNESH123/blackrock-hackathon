package com.hackathon.blackrock.model.request.response;

import com.hackathon.blackrock.domain.KPeriodResult;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReturnsResponse {
    private double totalTransactionAmount;
    private double totalCeiling;
    private List<KPeriodResult> savingsByDates;
}