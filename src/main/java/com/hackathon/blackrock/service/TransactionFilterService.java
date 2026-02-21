package com.hackathon.blackrock.service;

import com.hackathon.blackrock.model.request.FilterRequest;
import com.hackathon.blackrock.model.request.response.FilterResponse;

public interface TransactionFilterService {
    FilterResponse filter(FilterRequest request);

}
