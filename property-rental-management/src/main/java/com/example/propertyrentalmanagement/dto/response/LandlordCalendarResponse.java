package com.example.propertyrentalmanagement.dto.response;

import com.example.propertyrentalmanagement.entitites.AvailabilityCalendar;
import com.example.propertyrentalmanagement.entitites.Maintenance;
import com.example.propertyrentalmanagement.entitites.MaintenanceSchedule;
import com.example.propertyrentalmanagement.enums.MaintenanceScheduleStatus;
import com.example.propertyrentalmanagement.enums.MaintenanceStatus;
import com.example.propertyrentalmanagement.enums.Urgency;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record LandlordCalendarResponse(
        List<LandlordCalendarResponse.CalendarReservation> reservations,
        List<LandlordCalendarResponse.CalendarMaintenance> maintenances,
        List<LandlordCalendarResponse.CalendarMaintenanceSchedule> maintenanceSchedules
) {

    public record CalendarReservation(
            UUID id,
            UUID propertyId,
            String propertyTitle,
            String title,
            LocalDateTime timestampStart,
            LocalDateTime timestampEnd
    ) {
        public static CalendarReservation fromEntity(AvailabilityCalendar entry) {
            return new CalendarReservation(
                    entry.getId(),
                    entry.getProperty().getId(),
                    entry.getProperty().getTitle(),
                    entry.getBlockedReason(),
                    entry.getTimestampStart(),
                    entry.getTimestampEnd()
            );
        }
    }

    public record CalendarMaintenance(
            UUID id,
            UUID propertyId,
            String propertyTitle,
            String title,
            MaintenanceStatus maintenanceStatus,
            Urgency urgency,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd
    ) {
        public static CalendarMaintenance fromEntity(Maintenance m) {
            return new CalendarMaintenance(
                    m.getId(),
                    m.getProperty().getId(),
                    m.getProperty().getTitle(),
                    m.getTitle(),
                    m.getMaintenanceStatus(),
                    m.getUrgency(),
                    m.getScheduledStart(),
                    m.getScheduledEnd()
            );
        }
    }

    public record CalendarMaintenanceSchedule(
            UUID id,
            UUID propertyId,
            String propertyTitle,
            String title,
            MaintenanceScheduleStatus status,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd
    ) {
        public static CalendarMaintenanceSchedule fromEntity(MaintenanceSchedule ms) {
            LocalDateTime start = ms.getNextScheduledDate();
            LocalDateTime end = start.toLocalDate().atTime(23, 59, 59);
            return new CalendarMaintenanceSchedule(
                    ms.getId(),
                    ms.getProperty().getId(),
                    ms.getProperty().getTitle(),
                    ms.getTitle(),
                    ms.getStatus(),
                    start,
                    end
            );
        }
    }
}
