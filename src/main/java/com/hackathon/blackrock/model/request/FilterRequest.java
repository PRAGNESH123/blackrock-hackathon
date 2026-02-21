package com.hackathon.blackrock.model.request;

import com.hackathon.blackrock.domain.KPeriod;
import com.hackathon.blackrock.domain.PPeriod;
import com.hackathon.blackrock.domain.QPeriod;
import com.hackathon.blackrock.domain.Transaction;
import lombok.Data;

import java.util.List;

@Data
public class FilterRequest {
    private List<QPeriod> q;
    private List<PPeriod> p;
    private List<KPeriod> k;
    private double wage;
    private List<Transaction> transactions;
}