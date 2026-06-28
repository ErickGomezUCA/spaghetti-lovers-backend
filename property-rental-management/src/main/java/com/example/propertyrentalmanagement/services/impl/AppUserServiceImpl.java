package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.ChangePasswordRequest;
import com.example.propertyrentalmanagement.dto.request.CreateUserRequest;
import com.example.propertyrentalmanagement.dto.request.LoginRequest;
import com.example.propertyrentalmanagement.dto.request.UpdateUserRequest;
import com.example.propertyrentalmanagement.dto.response.AdminMonthlySummary;
import com.example.propertyrentalmanagement.dto.response.AuthResponse;
import com.example.propertyrentalmanagement.dto.response.UserProfileResponse;
import com.example.propertyrentalmanagement.dto.response.UserRatingsResponse;
import com.example.propertyrentalmanagement.dto.response.UserResponse;
import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.enums.PaymentType;
import com.example.propertyrentalmanagement.enums.ReservationStatus;
import com.example.propertyrentalmanagement.enums.UserRole;
import com.example.propertyrentalmanagement.exceptions.InvalidCredentials;
import com.example.propertyrentalmanagement.exceptions.UserAlreadyExistsException;
import com.example.propertyrentalmanagement.exceptions.UserNotFoundException;
import com.example.propertyrentalmanagement.repositories.AppUserRepository;
import com.example.propertyrentalmanagement.repositories.IdentityDocumentRepository;
import com.example.propertyrentalmanagement.repositories.PaymentRepository;
import com.example.propertyrentalmanagement.repositories.PropertyRepository;
import com.example.propertyrentalmanagement.repositories.ReservationRepository;
import com.example.propertyrentalmanagement.security.JwtService;
import com.example.propertyrentalmanagement.services.AppUserService;
import com.example.propertyrentalmanagement.services.RatingService;
import com.example.propertyrentalmanagement.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RatingService ratingService;
    private final PropertyRepository propertyRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final IdentityDocumentRepository identityDocumentRepository;

    @Override
    public AuthResponse createUser(CreateUserRequest userRequest) {
        if (appUserRepository.findByEmail(userRequest.email()).isPresent()) {
            throw new UserAlreadyExistsException("Email is already in use");
        }

        UserRole role = userRequest.role() == null ? UserRole.TENANT : userRequest.role();

        AppUser user = AppUser.builder()
                .name(userRequest.name())
                .email(userRequest.email())
                .passwordHash(passwordEncoder.encode(userRequest.password()))
                .role(role)
                .phone(userRequest.phone())
                .build();

        AppUser createdUser = appUserRepository.save(user);
        String token = jwtService.generateToken(createdUser);

        return AuthResponse.builder()
                .token(token)
                .user(UserResponse.fromEntity(createdUser))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        AppUser userFound = appUserRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new InvalidCredentials("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.password(), userFound.getPasswordHash())) {
            throw new InvalidCredentials("Invalid email or password");
        }

        String token = jwtService.generateToken(userFound);

        return AuthResponse.builder()
                .token(token)
                .user(UserResponse.fromEntity(userFound))
                .build();
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        AppUser userFound = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return UserResponse.fromEntity(userFound);
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        AppUser userFound = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return UserResponse.fromEntity(userFound);
    }

    @Override
    public UserRatingsResponse getUserRating(UUID userId) {
        return ratingService.getRatingsByUser(userId);
    }

    @Override
    public UserProfileResponse getUserProfile(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return buildUserProfileResponse(user);
    }

    @Override
    public Page<UserProfileResponse> getAllUsersForAdmin(int page, int pageSize, String sortBy, String sortOrder, UserRole role, String search) {
        Pageable pageable = PaginationUtils.getPageRequest(page, pageSize, sortBy, sortOrder);
        return appUserRepository.findWithFilters(role, search, pageable)
                .map(this::buildUserProfileResponse);
    }

    @Override
    public UserProfileResponse getUserProfileById(UUID userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return buildUserProfileResponse(user);
    }

    private UserProfileResponse buildUserProfileResponse(AppUser user) {
        UserRatingsResponse ratingsData = ratingService.getRatingsByUser(user.getId());

        int propertiesCount = user.getRole() == UserRole.LANDLORD
                ? propertyRepository.countByLandlordId(user.getId()).intValue()
                : 0;

        int reservationsCount;
        int completedReservationsCount;
        if (user.getRole() == UserRole.TENANT) {
            reservationsCount = reservationRepository.countByTenantId(user.getId()).intValue();
            completedReservationsCount = reservationRepository
                    .countByTenantIdAndReservationStatus(user.getId(), ReservationStatus.COMPLETED).intValue();
        } else if (user.getRole() == UserRole.LANDLORD) {
            reservationsCount = reservationRepository.countByPropertyLandlordId(user.getId()).intValue();
            completedReservationsCount = 0;
        } else {
            reservationsCount = 0;
            completedReservationsCount = 0;
        }

        String verificationStatus = identityDocumentRepository.findByUser_Id(user.getId())
                .map(doc -> doc.getDocumentStatus().name())
                .orElse(null);

        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt(),
                propertiesCount,
                reservationsCount,
                completedReservationsCount,
                ratingsData.totalRatings(),
                ratingsData.averageScore(),
                ratingsData.ratings(),
                verificationStatus
        );
    }

    @Override
    public UserResponse updateUser(UUID userId, UpdateUserRequest updateUserRequest) {
        AppUser userFound = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        AppUser userToUpdate = AppUser.builder()
                .id(userFound.getId())
                .name(updateUserRequest.name() != null ? updateUserRequest.name() : userFound.getName())
                .email(updateUserRequest.email() != null ? updateUserRequest.email() : userFound.getEmail())
                .phone(updateUserRequest.phone() != null ? updateUserRequest.phone() : userFound.getPhone())
                .passwordHash(userFound.getPasswordHash())
                .role(userFound.getRole())
                .createdAt(userFound.getCreatedAt())
                .build();

        AppUser updatedUser = appUserRepository.save(userToUpdate);
        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    public void changePassword(UUID userId, ChangePasswordRequest changePasswordRequest) {
        AppUser userFound = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(changePasswordRequest.oldPassword(), userFound.getPasswordHash())) {
            throw new InvalidCredentials("Current password is incorrect");
        }

        userFound.setPasswordHash(passwordEncoder.encode(changePasswordRequest.newPassword()));
        appUserRepository.save(userFound);
    }

    @Override
    public AdminMonthlySummary getAdminMonthlySummary(long activePropertiesCount) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        int daysInMonth = currentMonth.lengthOfMonth();

        long reservationsThisMonth = reservationRepository.countNonCancelledByCreatedAtBetween(
                ReservationStatus.CANCELLED, startDateTime, endDateTime);

        BigDecimal incomeThisMonth = paymentRepository.sumAmountByPaymentTypeInAndCreatedAtBetween(
                List.of(PaymentType.RESERVATION, PaymentType.EXTENSION), startDateTime, endDateTime);

        long totalNights = reservationRepository.sumTotalNightsNotCancelledByDateRange(
                ReservationStatus.CANCELLED, startDate, endDate);

        double averageOccupation = (activePropertiesCount > 0 && daysInMonth > 0)
                ? Math.round(((double) totalNights / (activePropertiesCount * daysInMonth)) * 100 * 10.0) / 10.0
                : 0.0;

        return new AdminMonthlySummary(reservationsThisMonth, incomeThisMonth, averageOccupation);
    }

}
