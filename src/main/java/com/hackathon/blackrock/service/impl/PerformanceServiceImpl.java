package com.hackathon.blackrock.service.impl;

import com.hackathon.blackrock.model.request.response.PerformanceResponse;
import com.hackathon.blackrock.service.PerformanceService;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.Instant;

@Service
public class PerformanceServiceImpl implements PerformanceService {
    private static final Instant APP_START_TIME = Instant.now();


    @Override
    public PerformanceResponse getMetrics() {
        String uptime = computeUptime();

        String memory = computeMemoryUsage();

        int threads = computeThreadCount();

        return PerformanceResponse.builder()
                .time(uptime)
                .memory(memory)
                .threads(threads)
                .build();
    }

    private String computeUptime() {
        Duration uptime = Duration.between(APP_START_TIME, Instant.now());

        long hours   = uptime.toHours();
        long minutes = uptime.toMinutesPart();
        long seconds = uptime.toSecondsPart();
        long millis  = uptime.toMillisPart();

        return String.format("%02d:%02d:%02d.%03d",
                hours, minutes, seconds, millis);
    }

    private String computeMemoryUsage() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        long usedBytes = memoryBean.getHeapMemoryUsage().getUsed();

        if (usedBytes < 0) usedBytes = 0;

        double usedMB = usedBytes / (1024.0 * 1024.0);

        return String.format("%.2f MB", usedMB);
    }


    private int computeThreadCount() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        return threadBean.getThreadCount();
    }

}

