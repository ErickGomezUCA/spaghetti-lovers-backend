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

            seedNotifications(users);

            log.info("¡Sembrado masivo completado con éxito!");
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
        for (AppUser user : users) {
            documents.add(IdentityDocument.builder()
                    .user(user)
                    .documentUrl("https://res.cloudinary.com/dbmchaesw/image/upload/v1782362419/property-rental/images/s6krv7hr9uznrnpa8r8s.jpg")
                    .documentStatus(DocumentStatus.values()[random.nextInt(DocumentStatus.values().length)])
                    .cloudinaryPublicId("sample_dui_" + user.getId())
                    .build());
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

        for (int i = 0; i < 25; i++) {
            AppUser randomLandlord = landlords.get(random.nextInt(landlords.size()));
            PropertyType randomType = PropertyType.values()[random.nextInt(PropertyType.values().length)];

            BigDecimal basePrice = BigDecimal.valueOf(faker.number().numberBetween(30, 250));

            Property property = Property.builder()
                    .landlord(randomLandlord)
                    .title(faker.lorem().sentence(3))
                    .description(faker.lorem().paragraph(2))
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
                    .rules(faker.lorem().sentence(5))
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
                    .build();

            reservations.add(reservation);
        }

        return reservationRepository.saveAll(reservations);    }

    private void seedReservationExtras(List<Reservation> reservations, List<AppUser> users) {
        List<AccessCode> accessCodes = new ArrayList<>();
        List<Payment> payments = new ArrayList<>();
        List<Fine> fines = new ArrayList<>();
        List<Rating> ratings = new ArrayList<>();
        List<Signature> signatures = new ArrayList<>();
        List<Contract> contracts = new ArrayList<>();

        for (Reservation res : reservations) {

            accessCodes.add(AccessCode.builder()
                    .property(res.getProperty())
                    .reservation(res)
                    .code(faker.number().digits(6)) // Ej: 482910
                    .codeType(CodeType.ACCESS_CODE)
                    .validFrom(res.getCheckInDate().atTime(14, 0)) // Check-in a las 2 PM
                    .validUntil(res.getCheckOutDate().atTime(11, 0)) // Check-out a las 11 AM
                    .isActive(res.getReservationStatus() == ReservationStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build());

            payments.add(Payment.builder()
                    .reservation(res)
                    .amount(res.getTotalPrice())
                    .paymentType(PaymentType.RESERVATION)
                    .paymentMethod(PaymentMethod.values()[random.nextInt(PaymentMethod.values().length)])
                    .createdAt(res.getCreatedAt())
                    .build());

            if (random.nextDouble() < 0.20) {
                Fine fine = Fine.builder()
                        .reservation(res)
                        .issuedBy(res.getProperty().getLandlord())
                        .fineType(FineType.values()[random.nextInt(FineType.values().length)])
                        .description(faker.lorem().sentence())
                        .amount(BigDecimal.valueOf(faker.number().numberBetween(20, 100)))
                        .issuedAt(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 5)))
                        .build();
                fines.add(fine);

                payments.add(Payment.builder()
                        .reservation(res)
                        .amount(fine.getAmount())
                        .paymentType(PaymentType.FINE)
                        .paymentMethod(PaymentMethod.CARD)
                        .createdAt(LocalDateTime.now())
                        .build());
            }

            Signature tenantSig = Signature.builder()
                    .hash(UUID.randomUUID().toString())
                    .signedTimestamp(LocalDateTime.now().minusDays(1))
                    .user(res.getTenant())
                    .build();

            Signature landlordSig = Signature.builder()
                    .hash(UUID.randomUUID().toString())
                    .signedTimestamp(LocalDateTime.now().minusDays(2))
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
                        .score(faker.number().numberBetween(3, 5)) // Reseñas realistas entre 3 y 5
                        .comment(faker.lorem().paragraph())
                        .createdAt(LocalDateTime.now())
                        .build());
            }
        }

        accessCodeRepository.saveAll(accessCodes);
        paymentRepository.saveAll(payments);
        fineRepository.saveAll(fines);
        signatureRepository.saveAll(signatures);
        contractRepository.saveAll(contracts);
        ratingRepository.saveAll(ratings);
    }

    private void seedNotifications(List<AppUser> users) {
        List<Notification> notifications = new ArrayList<>();
        for (AppUser user : users) {
            for (int i = 0; i < 3; i++) {
                notifications.add(Notification.builder()
                        .user(user)
                        .type(NotificationType.values()[random.nextInt(NotificationType.values().length)])
                        .title(faker.lorem().words(3).toString())
                        .message(faker.lorem().sentence(8))
                        .isRead(random.nextBoolean())
                        .createdAt(LocalDateTime.now().minusDays(faker.number().numberBetween(0, 10)))
                        .build());
            }
        }
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