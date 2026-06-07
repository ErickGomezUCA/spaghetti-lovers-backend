package com.example.propertyrentalmanagement.entitites;

import com.example.propertyrentalmanagement.enums.PropertyStatus;
import com.example.propertyrentalmanagement.enums.PropertyType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "property")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    private AppUser landlord;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    private String department;

    @Column(nullable = false)
    private String country;

    @Column(name = "base_price_per_night", nullable = false)
    private BigDecimal basePricePerNight;

    @Column(name = "cleaning_fee", nullable = false)
    private BigDecimal cleaningFee;

    @Column(name = "security_deposit_amount", nullable = false)
    private BigDecimal securityDepositAmount;

    @Column(name = "max_guests", nullable = false)
    private Integer maxGuests;

    private Integer bedrooms;

    private Integer bathrooms;

    @Column(name = "area_sqm")
    private BigDecimal areaSqm;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false)
    private PropertyType propertyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_status", nullable = false)
    private PropertyStatus propertyStatus;

    @Column(columnDefinition = "TEXT")
    private String rules;

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyPhoto> photos;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
