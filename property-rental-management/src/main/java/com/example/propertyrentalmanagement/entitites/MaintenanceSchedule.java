package com.example.propertyrentalmanagement.entitites;

import com.example.propertyrentalmanagement.enums.MaintenanceScheduleFrequency;
import com.example.propertyrentalmanagement.enums.MaintenanceScheduleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "maintenance_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduled_by_id", nullable = false)
    private AppUser scheduledBy;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceScheduleFrequency frequency;

    @Column(nullable = false)
    private int interval;

    @Column(name = "last_completed_at")
    private LocalDateTime lastCompletedAt;

    @Column(name = "next_scheduled_date", nullable = false)
    private LocalDateTime nextScheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceScheduleStatus status;
}
