package com.hackathon.blackrock.model.request.response;

import com.hackathon.blackrock.domain.InvalidTransaction;
import com.hackathon.blackrock.domain.Transaction;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ValidatorResponse {
    private List<Transaction> valid;
    private List<InvalidTransaction> invalid;
}
