package com.hackathon.blackrock.model.request.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PerformanceResponse {
    private String time;
    private String memory;
    private int threads;
}
