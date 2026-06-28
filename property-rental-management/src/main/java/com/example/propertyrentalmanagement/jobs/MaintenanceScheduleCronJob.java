package com.example.propertyrentalmanagement.jobs;

import com.example.propertyrentalmanagement.services.MaintenanceScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class MaintenanceScheduleCronJob {
    private static final Logger log = Logger.getLogger(MaintenanceScheduleCronJob.class.getName());
    private final MaintenanceScheduleService maintenanceScheduleService;


    // Development: 10 seconds: */10 * * * * *
    // Production: Everyday, at 00:00: 0 0 0 * * *
    @Scheduled(cron = "0 0 0 * * *")
    public void triggerDueMaintenanceSchedules() {
        log.info("Running maintenance schedule cron job...");
        maintenanceScheduleService.runDueSchedules();
    }
}
