package com.hackathon.blackrock.service;

import com.hackathon.blackrock.model.request.ValidatorRequest;
import com.hackathon.blackrock.model.request.response.ValidatorResponse;

public interface TransactionValidatorService {
    ValidatorResponse validate(ValidatorRequest request);

}
