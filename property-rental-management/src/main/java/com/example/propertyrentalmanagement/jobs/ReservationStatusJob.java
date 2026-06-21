package com.example.propertyrentalmanagement.jobs;

import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.ReservationStatus;
import com.example.propertyrentalmanagement.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationStatusJob {

    private final ReservationRepository reservationRepository;

    // Formato Cron: Segundo, Minuto, Hora, Día, Mes, Día de la semana
    @Scheduled(cron = "0 0 13 * * ?")
    @Transactional
    public void autoCheckInReservations() {
        LocalDate today = LocalDate.now();

        List<Reservation> checkInsToday = reservationRepository.findByCheckInDateAndReservationStatus(today, ReservationStatus.RESERVED);

        for (Reservation reservation : checkInsToday) {
            reservation.setReservationStatus(ReservationStatus.ACTIVE);
            reservation.setUpdatedAt(java.time.LocalDateTime.now());
        }

        reservationRepository.saveAll(checkInsToday);
        System.out.println("Auto Check-In completed for " + checkInsToday.size() + " reservations.");
    }
}
