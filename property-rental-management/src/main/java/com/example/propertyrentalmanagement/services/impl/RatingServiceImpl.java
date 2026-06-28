package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.CreateRatingRequest;
import com.example.propertyrentalmanagement.dto.response.RatingResponse;
import com.example.propertyrentalmanagement.dto.response.UserRatingsResponse;
import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.entitites.Rating;
import com.example.propertyrentalmanagement.entitites.Reservation;
import com.example.propertyrentalmanagement.enums.ReservationStatus;
import com.example.propertyrentalmanagement.enums.UserRole;
import com.example.propertyrentalmanagement.exceptions.ReservationNotFoundException;
import com.example.propertyrentalmanagement.exceptions.UserNotFoundException;
import com.example.propertyrentalmanagement.repositories.AppUserRepository;
import com.example.propertyrentalmanagement.repositories.RatingRepository;
import com.example.propertyrentalmanagement.repositories.ReservationRepository;
import com.example.propertyrentalmanagement.security.AuthenticatedUserProvider;
import com.example.propertyrentalmanagement.services.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.propertyrentalmanagement.entitites.Notification;
import com.example.propertyrentalmanagement.enums.NotificationType;
import com.example.propertyrentalmanagement.repositories.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final ReservationRepository reservationRepository;
    private final AppUserRepository appUserRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final NotificationRepository notificationRepository;

    @Override
    public RatingResponse createRating(CreateRatingRequest request) {

        AppUser reviewer = authenticatedUserProvider.getCurrentUser();

        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        if (reservation.getReservationStatus() != ReservationStatus.COMPLETED) {
            throw new IllegalStateException("Only completed reservations can be rated");
        }

        UUID reviewedId;

        if (reviewer.getRole() == UserRole.TENANT) {
            reviewedId = reservation.getProperty().getLandlord().getId();
        } else if (reviewer.getRole() == UserRole.LANDLORD) {
            reviewedId = reservation.getTenant().getId();
        } else {
            throw new IllegalStateException("Only Tenant or Landlord can create ratings");
        }

        AppUser reviewed = appUserRepository.findById(reviewedId)
                .orElseThrow(() -> new UserNotFoundException("User to rate not found"));

        boolean alreadyRated = ratingRepository.existsByReservationIdAndReviewerId(
                reservation.getId(),
                reviewer.getId()
        );
        if (alreadyRated) {
            throw new IllegalStateException("You have already rated this reservation");
        }

        Rating rating = Rating.builder()
                .reservation(reservation)
                .reviewer(reviewer)
                .reviewed(reviewed)
                .score(request.score())
                .comment(request.comment())
                .createdAt(LocalDateTime.now())
                .build();

        Rating saved = ratingRepository.save(rating);

        if (reviewed.getRole() == UserRole.LANDLORD) {
            Notification notification = Notification.builder()
                    .user(reviewed)
                    .reservation(reservation)
                    .type(NotificationType.INFO)
                    .title("Nueva calificación recibida")
                    .message(reviewer.getName()
                            + " te ha calificado con "
                            + request.score()
                            + " estrellas."
                            + (request.comment() != null && !request.comment().isBlank()
                            ? " Comentario: \"" + request.comment() + "\"."
                            : ""))
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
        }

        return RatingResponse.fromEntity(saved);


    }

    @Override
    public UserRatingsResponse getRatingsByUser(UUID userId) {

        appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Rating> ratings = ratingRepository.findByReviewedId(userId);

        Double average = ratingRepository.findAverageScoreByReviewedId(userId)
                .orElse(0.0);

        double roundedAverage = Math.round(average * 10.0) / 10.0;

        List<RatingResponse> ratingResponses = ratings.stream()
                .map(RatingResponse::fromEntity)
                .toList();

        return new UserRatingsResponse(
                userId,
                roundedAverage,
                ratings.size(),
                ratingResponses
        );
    }

    @Override
    public List<RatingResponse> getRatingsByReviewer(UUID reviewerId) {
        appUserRepository.findById(reviewerId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return ratingRepository.findByReviewerId(reviewerId)
                .stream()
                .map(RatingResponse::fromEntity)
                .toList();
    }
}
