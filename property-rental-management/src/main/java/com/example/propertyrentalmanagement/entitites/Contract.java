package com.example.propertyrentalmanagement.entitites;

import com.example.propertyrentalmanagement.enums.ContractStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contract")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @Column(name = "content_url", nullable = false)
    private String contentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_status", nullable = false)
    private ContractStatus contractStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_signature_id")
    private Signature tenantSignature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_signature_id")
    private Signature landlordSignature;

    @Column(name = "created_at_timestamp")
    private LocalDateTime createdAtTimestamp;

    @Column(name = "expires_at_timestamp")
    private LocalDateTime expiresAtTimestamp;
}
