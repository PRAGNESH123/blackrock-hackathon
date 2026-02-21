package com.hackathon.blackrock.model.request;

import com.hackathon.blackrock.domain.KPeriod;
import com.hackathon.blackrock.domain.PPeriod;
import com.hackathon.blackrock.domain.QPeriod;
import com.hackathon.blackrock.domain.Transaction;
import lombok.Data;

import java.util.List;

@Data
public class ReturnsRequest {
    private int age;
    private double wage;
    private double inflation;
    private List<QPeriod> q;
    private List<PPeriod> p;
    private List<KPeriod> k;
    private List<Transaction> transactions;
}
