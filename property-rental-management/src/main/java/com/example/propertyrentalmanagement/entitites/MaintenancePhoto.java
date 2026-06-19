package com.example.propertyrentalmanagement.entitites;

import com.example.propertyrentalmanagement.enums.MaintenancePhotoType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "maintenance_photo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenancePhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_request_id", nullable = false)
    private Maintenance maintenance;

    @Enumerated(EnumType.STRING)
    @Column(name = "photo_type", nullable = false)
    private MaintenancePhotoType photoType;

    @Column(nullable = false)
    private String url;

    @Column(name = "cloudinary_public_id")
    private String cloudinaryPublicId;
}
