package com.hackathon.blackrock.service;

import com.hackathon.blackrock.model.request.ExpenseRequest;
import com.hackathon.blackrock.model.request.response.TransactionResponse;

import java.util.List;

public interface TransactionParsingService {
    TransactionResponse parser(List<ExpenseRequest> expenses);

}
