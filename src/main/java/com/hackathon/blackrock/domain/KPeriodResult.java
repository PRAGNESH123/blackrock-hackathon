package com.hackathon.blackrock.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KPeriodResult {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")

    private LocalDateTime start;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")

    private LocalDateTime end;
    private double amount;
    private double profit;
    private double taxBenefit;
}
