package com.example.propertyrentalmanagement.entitites;

import com.example.propertyrentalmanagement.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private AppUser tenant;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "total_nights", nullable = false)
    private Integer totalNights;

    @Column(name = "base_total", nullable = false)
    private BigDecimal baseTotal;

    @Column(name = "cleaning_fee", nullable = false)
    private BigDecimal cleaningFee;

    @Column(name = "long_stay_discount")
    private BigDecimal longStayDiscount;

    @Column(name = "cancellation_penalty")
    private BigDecimal cancellationPenalty;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "guests_count", nullable = false)
    private Integer guestsCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false)
    private ReservationStatus reservationStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}
