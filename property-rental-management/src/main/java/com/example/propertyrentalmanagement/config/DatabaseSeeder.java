package com.example.propertyrentalmanagement.config;

import com.example.propertyrentalmanagement.entitites.*;
import com.example.propertyrentalmanagement.enums.*;
import com.example.propertyrentalmanagement.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final ReservationRepository reservationRepository;
    private final IdentityDocumentRepository identityDocumentRepository;
    private final PropertyPhotoRepository propertyPhotoRepository;
    private final MaintenanceScheduleRepository maintenanceScheduleRepository;
    private final AccessCodeRepository accessCodeRepository;
    private final PaymentRepository paymentRepository;
    private final FineRepository fineRepository;
    private final SignatureRepository signatureRepository;
    private final ContractRepository contractRepository;
    private final RatingRepository ratingRepository;
    private final NotificationRepository notificationRepository;
    private final AvailabilityCalendarRepository availabilityCalendarRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final MaintenancePhotoRepository maintenancePhotoRepository;

    private final PasswordEncoder passwordEncoder;

    private final Faker faker = new Faker(new Locale("es"));
    private final Random random = new Random();

    private final String[] cities = {"San Francisco Gotera", "San Miguel", "San Salvador", "Santa Tecla", "Perquín", "La Libertad"};
    private final String[] departments = {"Morazán", "San Miguel", "San Salvador", "La Libertad"};

    @Override
    public void run(String @NonNull ... args) {
        if (userRepository.count() == 0) {
            log.info("Iniciando el sembrado masivo de la base de datos...");

            List<AppUser> users = seedUsers();
            seedIdentityDocuments(users);

            List<Property> properties = seedProperties(users);
            seedPropertyExtras(properties, users);

            List<Reservation> reservations = seedReservations(users, properties);
            seedReservationExtras(reservations, users);

            seedAvailabilityAndMaintenance(reservations, properties, users);

            log.info("¡Sembrado masivo completado con éxito!");
        } else {
            log.info("La base de datos ya contiene información. Se omite el seeder.");
        }
    }

    private List<AppUser> seedUsers() {
        List<AppUser> users = new ArrayList<>();

        String plainPassword = "Password.123";
        String dynamicPasswordHash = passwordEncoder.encode(plainPassword);

        for (int i = 0; i < 2; i++) {
            users.add(createUser(UserRole.ADMIN, dynamicPasswordHash));
        }

        for (int i = 0; i < 5; i++) {
            users.add(createUser(UserRole.LANDLORD, dynamicPasswordHash));
        }

        for (int i = 0; i < 7; i++) {
            users.add(createUser(UserRole.TENANT, dynamicPasswordHash));
        }

        return userRepository.saveAll(users);
    }

    private AppUser createUser(UserRole role, String passwordHash) {
        return AppUser.builder()
                .name(faker.name().fullName())
                .email(faker.internet().emailAddress())
                .passwordHash(passwordHash)
                .role(role)
                .phone(faker.phoneNumber().cellPhone())
                .build();
    }

    private void seedIdentityDocuments(List<AppUser> users) {
        List<IdentityDocument> documents = new ArrayList<>();

        AppUser adminUser = users.stream()
                .filter(u -> u.getRole() == UserRole.ADMIN)
                .findFirst()
                .orElseThrow();

        List<AppUser> targetUsers = new ArrayList<>(users.stream()
                .filter(u -> u.getRole() == UserRole.TENANT || u.getRole() == UserRole.LANDLORD)
                .toList());

        Collections.shuffle(targetUsers);

        String realDuiUrl = "https://res.cloudinary.com/dbmchaesw/image/upload/v1782362419/property-rental/images/s6krv7hr9uznrnpa8r8s.jpg";
        String[] rejectionReasons = {
                "La imagen está borrosa y no se pueden leer los datos con claridad.",
                "El documento de identidad se encuentra vencido.",
                "Falta adjuntar el reverso del documento."
        };

        for (int i = 0; i < targetUsers.size(); i++) {
            AppUser user = targetUsers.get(i);

            IdentityDocument.IdentityDocumentBuilder docBuilder = IdentityDocument.builder()
                    .user(user)
                    .documentUrl(realDuiUrl);

            if (i < 7) {
                LocalDateTime reviewedDate = LocalDateTime.now().minusDays(faker.number().numberBetween(10, 30));
                LocalDateTime createdDate = reviewedDate.minusDays(faker.number().numberBetween(1, 3));

                docBuilder.documentStatus(DocumentStatus.VERIFIED)
                        .reviewedBy(adminUser)
                        .reviewedAt(reviewedDate)
                        .createdAt(createdDate)
                        .cloudinaryPublicId("sample_dui_verified_" + user.getId());
            } else if (i < 10) {
                docBuilder.documentStatus(DocumentStatus.PENDING)
                        .createdAt(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 5)))
                        .cloudinaryPublicId("sample_dui_pending_" + user.getId());
            } else {
                LocalDateTime reviewedDate = LocalDateTime.now().minusDays(faker.number().numberBetween(1, 5));
                LocalDateTime createdDate = reviewedDate.minusHours(faker.number().numberBetween(2, 48));

                docBuilder.documentStatus(DocumentStatus.REJECTED)
                        .reviewedBy(adminUser)
                        .reviewedAt(reviewedDate)
                        .rejectionReason(rejectionReasons[random.nextInt(rejectionReasons.length)])
                        .createdAt(createdDate)
                        .cloudinaryPublicId("sample_dui_rejected_" + user.getId());
            }

            documents.add(docBuilder.build());
        }

        identityDocumentRepository.saveAll(documents);
    }

    private void seedPropertyExtras(List<Property> properties, List<AppUser> users) {
        List<PropertyPhoto> photos = new ArrayList<>();
        List<MaintenanceSchedule> schedules = new ArrayList<>();

        String[] cloudinaryPublicIds = {
                "property-rental/images/cxobnju2cbujcdmnityv",
                "property-rental/images/c7x8bl9whfmiwcp4x9h5"
        };
        String baseUrl = "https://res.cloudinary.com/dbmchaesw/image/upload/";

        Object[][] maintenanceTemplates = {
                {"Fumigación y Control de Plagas", "Aplicación de tratamiento preventivo contra insectos y roedores en áreas comunes.", MaintenanceScheduleFrequency.MONTHLY, 3, MaintenanceScheduleStatus.ACTIVE},
                {"Limpieza de Cisterna y Filtros", "Lavado profundo del tanque de agua principal y sustitución de filtros hidráulicos.", MaintenanceScheduleFrequency.YEARLY, 1, MaintenanceScheduleStatus.SCHEDULED},
                {"Inspección de Seguridad", "Revisión obligatoria de alarmas de humo, extintores y luces de emergencia de la propiedad.", MaintenanceScheduleFrequency.WEEKLY, 2, MaintenanceScheduleStatus.DONE},
                {"Mantenimiento de Aire Acondicionado", "Limpieza profunda de evaporadores, condensadores y recarga preventiva de gas refrigerante.", MaintenanceScheduleFrequency.MONTHLY, 6, MaintenanceScheduleStatus.SCHEDULED},
                {"Revisión de Ascensor / Accesos", "Mantenimiento preventivo de portones eléctricos y sistemas de acceso inteligente.", MaintenanceScheduleFrequency.MONTHLY, 1, MaintenanceScheduleStatus.ACTIVE}
        };

        for (Property prop : properties) {

            for (int i = 0; i < 4; i++) {
                String chosenPublicId = cloudinaryPublicIds[random.nextInt(cloudinaryPublicIds.length)];
                photos.add(PropertyPhoto.builder()
                        .property(prop)
                        .url(baseUrl + chosenPublicId + ".jpg")
                        .cloudinaryPublicId(chosenPublicId)
                        .build());
            }

            List<Integer> templateIndices = new ArrayList<>(List.of(0, 1, 2, 3, 4));
            java.util.Collections.shuffle(templateIndices);

            for (int j = 0; j < 2; j++) {
                int index = templateIndices.get(j);
                Object[] template = maintenanceTemplates[index];

                String title = (String) template[0];
                String description = (String) template[1];
                MaintenanceScheduleFrequency frequency = (MaintenanceScheduleFrequency) template[2];
                int interval = (Integer) template[3];
                MaintenanceScheduleStatus status = (MaintenanceScheduleStatus) template[4];

                LocalDateTime lastCompleted = null;
                if (status == MaintenanceScheduleStatus.DONE) {
                    lastCompleted = LocalDateTime.now().minusDays(faker.number().numberBetween(5, 30));
                }

                int daysToNext = (status == MaintenanceScheduleStatus.DONE)
                        ? faker.number().numberBetween(30, 90)
                        : faker.number().numberBetween(2, 25);

                schedules.add(MaintenanceSchedule.builder()
                        .property(prop)
                        .scheduledBy(prop.getLandlord())
                        .title(title + " - " + prop.getTitle())
                        .description(description)
                        .frequency(frequency)
                        .interval(interval)
                        .lastCompletedAt(lastCompleted)
                        .nextScheduledDate(LocalDateTime.now().plusDays(daysToNext))
                        .status(status)
                        .build());
            }
        }

        propertyPhotoRepository.saveAll(photos);
        maintenanceScheduleRepository.saveAll(schedules);
    }

    private List<Property> seedProperties(List<AppUser> allUsers) {
        List<Property> properties = new ArrayList<>();
        List<AppUser> landlords = allUsers.stream()
                .filter(u -> u.getRole() == UserRole.LANDLORD)
                .toList();

        String[] realTitles = {
                "Cabaña Rústica Los Pinos", "Apartamento Ejecutivo Centro", "Casa Familiar con Amplio Jardín",
                "Villa Exclusiva con Piscina", "Loft Moderno Minimalista", "Hostal Ruta de Paz - Habitación Privada",
                "Quinta de Descanso La Arboleda", "Estudio Céntrico Ideal para Viajeros", "Cabaña de Montaña El Mirador",
                "Penthouse con Vista a la Ciudad", "Casa de Playa Brisa Marina", "Condominio Seguro 24/7",
                "Habitación Cómoda cerca de Universidad", "Duplex Espacioso para Familias", "Cabaña Eco-Amigable"
        };

        for (int i = 0; i < 25; i++) {
            AppUser randomLandlord = landlords.get(random.nextInt(landlords.size()));
            PropertyType randomType = PropertyType.values()[random.nextInt(PropertyType.values().length)];
            BigDecimal basePrice = BigDecimal.valueOf(faker.number().numberBetween(30, 250));

            String title = realTitles[random.nextInt(realTitles.length)];

            Property property = Property.builder()
                    .landlord(randomLandlord)
                    .title(title + " en " + cities[random.nextInt(cities.length)])
                    .description("Excelente propiedad equipada con todas las comodidades básicas. " +
                            "Ideal para estancias cortas o largas. Ubicación estratégica cerca de zonas comerciales y turísticas.")
                    .address(faker.address().streetAddress())
                    .city(cities[random.nextInt(cities.length)])
                    .department(departments[random.nextInt(departments.length)])
                    .country("El Salvador")
                    .basePricePerNight(basePrice)
                    .cleaningFee(BigDecimal.valueOf(15.00))
                    .securityDepositAmount(basePrice.multiply(BigDecimal.valueOf(2)))
                    .maxGuests(faker.number().numberBetween(1, 10))
                    .bedrooms(faker.number().numberBetween(1, 5))
                    .bathrooms(faker.number().numberBetween(1, 4))
                    .areaSqm(BigDecimal.valueOf(faker.number().numberBetween(40, 300)))
                    .propertyType(randomType)
                    .propertyStatus(PropertyStatus.ACTIVE)
                    .rules("Prohibido fumar en interiores. No se permiten mascotas grandes. Respetar horas de silencio (10 PM - 6 AM).")
                    .build();

            properties.add(property);
        }

        return propertyRepository.saveAll(properties);
    }

    private List<Reservation> seedReservations(List<AppUser> allUsers, List<Property> properties) {
        List<Reservation> reservations = new ArrayList<>();
        List<AppUser> tenants = allUsers.stream()
                .filter(u -> u.getRole() == UserRole.TENANT)
                .toList();

        for (int i = 0; i < 50; i++) {
            AppUser randomTenant = tenants.get(random.nextInt(tenants.size()));
            Property randomProperty = properties.get(random.nextInt(properties.size()));
            ReservationStatus randomStatus = ReservationStatus.values()[random.nextInt(ReservationStatus.values().length)];

            int daysToAdd = faker.number().numberBetween(-30, 60);
            LocalDate checkIn = LocalDate.now().plusDays(daysToAdd);
            int totalNights = faker.number().numberBetween(1, 14);
            LocalDate checkOut = checkIn.plusDays(totalNights);

            LocalDateTime createdAt = checkIn.minusDays(faker.number().numberBetween(1, 15)).atStartOfDay();

            BigDecimal baseTotal = randomProperty.getBasePricePerNight().multiply(BigDecimal.valueOf(totalNights));
            BigDecimal cleaningFee = randomProperty.getCleaningFee();
            BigDecimal totalPrice = baseTotal.add(cleaningFee);

            Reservation reservation = Reservation.builder()
                    .property(randomProperty)
                    .tenant(randomTenant)
                    .checkInDate(checkIn)
                    .checkOutDate(checkOut)
                    .totalNights(totalNights)
                    .baseTotal(baseTotal)
                    .cleaningFee(cleaningFee)
                    .totalPrice(totalPrice)
                    .guestsCount(faker.number().numberBetween(1, randomProperty.getMaxGuests()))
                    .reservationStatus(randomStatus)
                    .createdAt(createdAt)
                    .updatedAt(createdAt)
                    .build();

            reservations.add(reservation);
        }

        return reservationRepository.saveAll(reservations);
    }

    private void seedReservationExtras(List<Reservation> reservations, List<AppUser> users) {
        List<AccessCode> accessCodes = new ArrayList<>();
        List<Payment> payments = new ArrayList<>();
        List<Fine> fines = new ArrayList<>();
        List<Rating> ratings = new ArrayList<>();
        List<Signature> signatures = new ArrayList<>();
        List<Contract> contracts = new ArrayList<>();
        List<Notification> notifications = new ArrayList<>();

        for (Reservation res : reservations) {

            boolean codesActive = res.getReservationStatus() == ReservationStatus.ACTIVE || res.getReservationStatus() == ReservationStatus.RESERVED;

            accessCodes.add(AccessCode.builder()
                    .property(res.getProperty())
                    .reservation(res)
                    .code(faker.number().digits(6))
                    .codeType(CodeType.ACCESS_CODE)
                    .validFrom(res.getCheckInDate().atTime(14, 0))
                    .validUntil(res.getCheckOutDate().atTime(11, 0))
                    .isActive(codesActive)
                    .createdAt(res.getCreatedAt())
                    .build());

            accessCodes.add(AccessCode.builder()
                    .property(res.getProperty())
                    .reservation(res)
                    .code(faker.number().digits(8))
                    .codeType(CodeType.RECOVERY_CODE)
                    .validFrom(res.getCheckInDate().atTime(14, 0))
                    .validUntil(res.getCheckOutDate().atTime(11, 0))
                    .isActive(codesActive)
                    .createdAt(res.getCreatedAt())
                    .build());

            payments.add(Payment.builder()
                    .reservation(res)
                    .amount(res.getTotalPrice().subtract(res.getProperty().getSecurityDepositAmount()))
                    .paymentType(PaymentType.RESERVATION)
                    .paymentMethod(PaymentMethod.values()[random.nextInt(PaymentMethod.values().length)])
                    .createdAt(res.getCreatedAt())
                    .build());

            payments.add(Payment.builder()
                    .reservation(res)
                    .amount(res.getProperty().getSecurityDepositAmount())
                    .paymentType(PaymentType.GUARANTEE_DEPOSIT)
                    .paymentMethod(PaymentMethod.CARD)
                    .refundAmount(res.getReservationStatus() == ReservationStatus.COMPLETED ? res.getProperty().getSecurityDepositAmount() : null)
                    .refundedAt(res.getReservationStatus() == ReservationStatus.COMPLETED ? LocalDateTime.now() : null)
                    .createdAt(res.getCreatedAt())
                    .build());

            notifications.add(Notification.builder()
                    .user(res.getProperty().getLandlord())
                    .reservation(res)
                    .type(NotificationType.INFO)
                    .title("Nueva reserva confirmada")
                    .message("El inquilino " + res.getTenant().getName() + " ha reservado " + res.getProperty().getTitle())
                    .isRead(random.nextBoolean())
                    .createdAt(res.getCreatedAt())
                    .build());

            Signature tenantSig = Signature.builder()
                    .hash(UUID.randomUUID().toString())
                    .signedTimestamp(res.getCreatedAt().plusHours(1))
                    .user(res.getTenant())
                    .build();

            Signature landlordSig = Signature.builder()
                    .hash(UUID.randomUUID().toString())
                    .signedTimestamp(res.getCreatedAt().plusHours(2))
                    .user(res.getProperty().getLandlord())
                    .build();

            signatures.add(tenantSig);
            signatures.add(landlordSig);

            contracts.add(Contract.builder()
                    .reservation(res)
                    .contentUrl("https://storage.example.com/contracts/" + res.getId() + ".pdf")
                    .contractStatus(ContractStatus.SIGNED)
                    .tenantSignature(tenantSig)
                    .landlordSignature(landlordSig)
                    .createdAtTimestamp(res.getCreatedAt())
                    .expiresAtTimestamp(res.getCheckOutDate().atTime(23, 59))
                    .build());

            if (res.getReservationStatus() == ReservationStatus.COMPLETED) {
                ratings.add(Rating.builder()
                        .reservation(res)
                        .reviewer(res.getTenant())
                        .reviewed(res.getProperty().getLandlord())
                        .score(faker.number().numberBetween(3, 5))
                        .comment("Excelente estadía, el lugar estaba muy limpio y el anfitrión fue muy amable.")
                        .createdAt(LocalDateTime.now())
                        .build());
            }

            if (res.getReservationStatus() == ReservationStatus.ACTIVE || res.getReservationStatus() == ReservationStatus.RESERVED) {
                res.getProperty().setPropertyStatus(PropertyStatus.RESERVED);
                propertyRepository.save(res.getProperty());
            }
        }

        List<Reservation> reservationsForFines = new ArrayList<>(reservations);
        java.util.Collections.shuffle(reservationsForFines);
        int finesToCreate = Math.min(20, reservationsForFines.size());

        for (int i = 0; i < finesToCreate; i++) {
            Reservation res = reservationsForFines.get(i);

            BigDecimal fineAmount = BigDecimal.valueOf(faker.number().numberBetween(15, 150));
            FineType randomFineType = FineType.values()[random.nextInt(FineType.values().length)];

            Payment finePayment = Payment.builder()
                    .reservation(res)
                    .amount(fineAmount)
                    .paymentType(PaymentType.FINE)
                    .paymentMethod(res.getReservationStatus() == ReservationStatus.COMPLETED ? PaymentMethod.CARD : PaymentMethod.PENDING)
                    .createdAt(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 5)))
                    .build();

            finePayment = paymentRepository.save(finePayment);

            Fine fine = Fine.builder()
                    .reservation(res)
                    .issuedBy(res.getProperty().getLandlord())
                    .fineType(randomFineType)
                    .description("Infracción detectada: " + randomFineType.name() + ". Se requiere el pago inmediato.")
                    .amount(fineAmount)
                    .payment(finePayment)
                    .issuedAt(finePayment.getCreatedAt())
                    .resolvedAt(res.getReservationStatus() == ReservationStatus.COMPLETED ? LocalDateTime.now() : null)
                    .build();

            fines.add(fine);

            notifications.add(Notification.builder()
                    .user(res.getTenant())
                    .reservation(res)
                    .type(NotificationType.INFO)
                    .title("Nueva multa emitida")
                    .message("Se ha emitido un cargo por $" + fineAmount + " debido a: " + randomFineType.name())
                    .isRead(false)
                    .createdAt(fine.getIssuedAt())
                    .build());
        }

        accessCodeRepository.saveAll(accessCodes);
        paymentRepository.saveAll(payments);
        fineRepository.saveAll(fines);
        signatureRepository.saveAll(signatures);
        contractRepository.saveAll(contracts);
        ratingRepository.saveAll(ratings);
        notificationRepository.saveAll(notifications);
    }

    private void seedAvailabilityAndMaintenance(List<Reservation> reservations, List<Property> properties, List<AppUser> users) {
        List<AvailabilityCalendar> calendarBlocks = new ArrayList<>();
        List<Maintenance> maintenanceRequests = new ArrayList<>();
        List<MaintenancePhoto> maintenancePhotos = new ArrayList<>();

        for (Reservation res : reservations) {
            calendarBlocks.add(AvailabilityCalendar.builder()
                    .property(res.getProperty())
                    .timestampStart(res.getCheckInDate().atTime(14, 0))
                    .timestampEnd(res.getCheckOutDate().atTime(11, 0))
                    .blockType(BlockType.RESERVATION)
                    .reservation(res)
                    .build());
        }

        List<Reservation> validReservations = reservations.stream()
                .filter(r -> r.getReservationStatus() == ReservationStatus.ACTIVE || r.getReservationStatus() == ReservationStatus.COMPLETED)
                .toList();

        String[][] maintenanceIssues = {
                {"Fuga de agua en lavamanos", "Huéspedes en ruta turística por la zona reportaron que el lavamanos del baño principal tiene una fuga constante."},
                {"Aire Acondicionado sin enfriar", "El equipo enciende pero no enfría la habitación. Urgente por las altas temperaturas."},
                {"Fallo en cerradura inteligente", "El código de acceso a veces no es reconocido por el panel frontal."},
                {"Refrigeradora no congela", "Los inquilinos reportan que los alimentos no se mantienen fríos en el compartimento superior."}
        };

        for (int i = 0; i < 10; i++) {
            Reservation randomRes = validReservations.get(random.nextInt(validReservations.size()));
            String[] issue = maintenanceIssues[random.nextInt(maintenanceIssues.length)];

            Maintenance maintenance = Maintenance.builder()
                    .property(randomRes.getProperty())
                    .reservation(randomRes)
                    .reportedBy(randomRes.getTenant())
                    .title(issue[0])
                    .description(issue[1])
                    .urgency(Urgency.values()[random.nextInt(Urgency.values().length)])
                    .scheduledStart(LocalDateTime.now().plusDays(1))
                    .scheduledEnd(LocalDateTime.now().plusDays(2))
                    .maintenanceStatus(MaintenanceStatus.values()[random.nextInt(MaintenanceStatus.values().length)])
                    .build();

            if (maintenance.getMaintenanceStatus() == MaintenanceStatus.RESOLVED) {
                maintenance.setResolutionNotes("Se reemplazó la pieza defectuosa y se verificó el funcionamiento con el inquilino.");
            }

            maintenanceRequests.add(maintenance);

            maintenancePhotos.add(MaintenancePhoto.builder()
                    .maintenance(maintenance)
                    .photoType(MaintenancePhotoType.REQUEST)
                    .url("https://res.cloudinary.com/dbmchaesw/image/upload/property-rental/images/cxobnju2cbujcdmnityv.jpg")
                    .cloudinaryPublicId("property-rental/images/cxobnju2cbujcdmnityv")
                    .build());

            calendarBlocks.add(AvailabilityCalendar.builder()
                    .property(randomRes.getProperty())
                    .timestampStart(maintenance.getScheduledStart())
                    .timestampEnd(maintenance.getScheduledEnd())
                    .blockType(BlockType.MAINTENANCE)
                    .blockedReason("Reparación: " + issue[0])
                    .maintenance(maintenance)
                    .build());
        }

        maintenanceRepository.saveAll(maintenanceRequests);
        maintenancePhotoRepository.saveAll(maintenancePhotos);
        availabilityCalendarRepository.saveAll(calendarBlocks);
    }
}