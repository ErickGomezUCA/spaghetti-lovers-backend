package com.example.propertyrentalmanagement.entitites;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "signature")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Signature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String hash;

    @Column(name = "signed_timestamp", nullable = false)
    private LocalDateTime signedTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
}
