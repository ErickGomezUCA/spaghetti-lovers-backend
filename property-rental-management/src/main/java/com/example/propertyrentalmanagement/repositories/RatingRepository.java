package com.example.propertyrentalmanagement.repositories;

import com.example.propertyrentalmanagement.entitites.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {
    List<Rating> findByReviewedId(UUID reviewedId);

    boolean existsByReservationIdAndReviewerId(UUID reservation, UUID reviewerId);

    /*@Query("SELECT AVG(r.score) FROM Rating r WHERE r.reviewed.id = :reviewedId")
    Optional<Double> findAverageScoreByReviewedId(@Param("reviewedId") UUID reviewedId);*/
}
