package com.hackathon.blackrock.service;

import com.hackathon.blackrock.model.request.ReturnsRequest;
import com.hackathon.blackrock.model.request.response.ReturnsResponse;

public interface ReturnsCalculationService {
    ReturnsResponse calculate(ReturnsRequest request, boolean isNPS);


}
