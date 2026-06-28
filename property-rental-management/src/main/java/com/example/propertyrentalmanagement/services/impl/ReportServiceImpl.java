package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.response.PropertyReportResponse;
import com.example.propertyrentalmanagement.entitites.Property;
import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.PaymentType;
import com.example.propertyrentalmanagement.enums.ReservationStatus;
import com.example.propertyrentalmanagement.enums.UserRole;
import com.example.propertyrentalmanagement.exceptions.NotResourceOwnerException;
import com.example.propertyrentalmanagement.exceptions.PropertyNotFound;
import com.example.propertyrentalmanagement.repositories.PaymentRepository;
import com.example.propertyrentalmanagement.repositories.PropertyRepository;
import com.example.propertyrentalmanagement.repositories.ReservationRepository;
import com.example.propertyrentalmanagement.security.AuthenticatedUserProvider;
import com.example.propertyrentalmanagement.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final PropertyRepository propertyRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    public PropertyReportResponse getPropertyReport(UUID propertyId, LocalDate startDate, LocalDate endDate) {

        // Validar que startDate < endDate
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        // Verificar que la propiedad exista
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFound("Property not found"));

        // Verificar que sea el Landlord dueño o Admin
        var currentUser = authenticatedUserProvider.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        boolean isOwner = property.getLandlord().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new NotResourceOwnerException("You are not allowed to access this report");
        }

        // Consultar reservas no canceladas dentro del rango
        List<Reservation> reservations = reservationRepository
                .findByPropertyIdAndStatusNotCancelledAndDateRange(
                        propertyId,
                        ReservationStatus.CANCELLED,
                        startDate,
                        endDate
                );

        // Calcular métricas
        // total_noches_ocupadas = SUM(total_nights)
        int totalNightsOccupied = reservations.stream()
                .mapToInt(Reservation::getTotalNights)
                .sum();

        // total_noches_disponibles = días del período
        long totalNightsAvailable = ChronoUnit.DAYS.between(startDate, endDate);

        // tasa_ocupacion = (noches ocupadas / noches disponibles) * 100
        double occupancyRate = totalNightsAvailable > 0
                ? Math.round(((double) totalNightsOccupied / totalNightsAvailable) * 100 * 10.0) / 10.0
                : 0.0;

        // ingresos_base = SUM(base_total)
        BigDecimal ingresoBase = reservations.stream()
                .map(Reservation::getBaseTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ingresos_limpieza = SUM(cleaning_fee)
        BigDecimal ingresoLimpieza = reservations.stream()
                .map(Reservation::getCleaningFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // penalizaciones = SUM(cancellation_penalty)
        BigDecimal penalizaciones = reservations.stream()
                .filter(r -> r.getCancellationPenalty() != null)
                .map(Reservation::getCancellationPenalty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // total_ingresos desde payments (RESERVATION + EXTENSION)
        BigDecimal totalIngresos;
        if (reservations.isEmpty()) {
            totalIngresos = BigDecimal.ZERO;
        } else {
            List<UUID> reservationIds = reservations.stream()
                    .map(Reservation::getId)
                    .toList();

            totalIngresos = paymentRepository.sumAmountByReservationIdsAndPaymentTypes(
                    reservationIds,
                    List.of(PaymentType.RESERVATION, PaymentType.EXTENSION)
            );
        }

        return new PropertyReportResponse(
                propertyId,
                property.getTitle(),
                new PropertyReportResponse.PeriodResponse(
                        startDate.toString(),
                        endDate.toString()
                ),
                occupancyRate,
                totalNightsOccupied,
                reservations.size(),
                new PropertyReportResponse.RevenueResponse(
                        ingresoBase,
                        ingresoLimpieza,
                        penalizaciones,
                        totalIngresos
                )
        );
    }

    @Override
    public List<PropertyReportResponse> getAllPropertiesReport(LocalDate startDate, LocalDate endDate, UUID landlordId) {
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        var currentUser = authenticatedUserProvider.getCurrentUser();
        List<Property> properties;

        if (currentUser.getRole() == UserRole.ADMIN) {
            // Admin: si especifica landlordId filtra por ese landlord, si no ve todas
            if (landlordId != null) {
                properties = propertyRepository.findAllByLandlordId(landlordId);
            } else {
                properties = propertyRepository.findAll();
            }
        } else {
            // Landlord: siempre solo ve las suyas, ignorar landlordId
            properties = propertyRepository.findAllByLandlordId(currentUser.getId());
        }

        return properties.stream()
                .map(property -> getPropertyReport(property.getId(), startDate, endDate))
                .toList();
    }
}
