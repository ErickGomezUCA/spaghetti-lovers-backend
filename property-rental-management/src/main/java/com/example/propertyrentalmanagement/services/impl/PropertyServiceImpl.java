package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.dto.request.AttachPhotoRequest;
import com.example.propertyrentalmanagement.dto.request.CreatePropertyRequest;
import com.example.propertyrentalmanagement.dto.request.UpdatePropertyRequest;
import com.example.propertyrentalmanagement.dto.response.PropertyResponse;
import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.entitites.Property;
import com.example.propertyrentalmanagement.entitites.PropertyPhoto;
import com.example.propertyrentalmanagement.enums.PropertyStatus;
import com.example.propertyrentalmanagement.exceptions.NotResourceOwnerException;
import com.example.propertyrentalmanagement.exceptions.PropertyNotFound;
import com.example.propertyrentalmanagement.exceptions.UserNotFoundException;
import com.example.propertyrentalmanagement.repositories.AppUserRepository;
import com.example.propertyrentalmanagement.repositories.PropertyPhotoRepository;
import com.example.propertyrentalmanagement.repositories.PropertyRepository;
import com.example.propertyrentalmanagement.security.AuthenticatedUserProvider;
import com.example.propertyrentalmanagement.services.CloudinaryService;
import com.example.propertyrentalmanagement.services.PropertyService;
import com.example.propertyrentalmanagement.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {
    private final PropertyRepository propertyRepository;
    private final AppUserRepository appUserRepository;
    private final PropertyPhotoRepository propertyPhotoRepository;
    private final AuthenticatedUserProvider authProvider;
    private final CloudinaryService cloudinaryService;

    @Override
    public PropertyResponse createProperty(CreatePropertyRequest propertyRequest) {
        AppUser authUser = authProvider.getCurrentUser();

        Property property = Property.builder()
                .landlord(authUser)
                .title(propertyRequest.title())
                .description(propertyRequest.description())
                .address(propertyRequest.address())
                .city(propertyRequest.city())
                .department(propertyRequest.department())
                .country(propertyRequest.country())
                .basePricePerNight(propertyRequest.basePricePerNight())
                .cleaningFee(propertyRequest.cleaningFee())
                .securityDepositAmount(propertyRequest.securityDepositAmount())
                .maxGuests(propertyRequest.maxGuests())
                .bedrooms(propertyRequest.bedrooms())
                .bathrooms(propertyRequest.bathrooms())
                .areaSqm(propertyRequest.areaSqm())
                .propertyType(propertyRequest.propertyType())
                .propertyStatus(PropertyStatus.ACTIVE)
                .rules(propertyRequest.rules())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Property createdProperty = propertyRepository.save(property);

        // Attach photos if provided
        if (propertyRequest.photoUrls() != null && !propertyRequest.photoUrls().isEmpty()) {
            return attachPhotosToPropertyByList(createdProperty, propertyRequest.photoUrls());
        }

        return PropertyResponse.fromEntity(createdProperty);
    }

    @Override
    public PropertyResponse attachPhotosToProperty(UUID propertyId, AttachPhotoRequest photoUrlsRequest) {
        AppUser authUser = authProvider.getCurrentUser();
        Property propertyFound = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFound("Property not found"));
        checkPropertyOwnership(propertyFound, authUser);

        return attachPhotosToPropertyByList(propertyFound, photoUrlsRequest.photoUrls());
    }

    @Override
    public PropertyResponse getPropertyById(UUID propertyId) {
        Property propertyFound = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFound("Property not found"));
        return PropertyResponse.fromEntity(propertyFound);
    }

    @Override
    public Page<PropertyResponse> getAllProperties(int page, int pageSize, String sortBy, String sortOrder) {
        Pageable pageable = PaginationUtils.getPageRequest(page, pageSize, sortBy, sortOrder);
        
        return propertyRepository.findAll(pageable).map(PropertyResponse::fromEntity);
    }

    @Override
    public Page<PropertyResponse> getPropertiesByLandlordId(UUID landlordId, int page, int pageSize, String sortBy, String sortOrder) {
        appUserRepository.findById(landlordId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Pageable pageable = PaginationUtils.getPageRequest(page, pageSize, sortBy, sortOrder);

        return propertyRepository.findAllByLandlordId(landlordId, pageable).map(PropertyResponse::fromEntity);
    }

    @Override
    public PropertyResponse updateProperty(UUID propertyId, UpdatePropertyRequest propertyRequest) {
        AppUser authUser = authProvider.getCurrentUser();

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFound("Property not found"));

        checkPropertyOwnership(property, authUser);

        // TODO: Include list of photos here?

        Property valuesToUpdate = Property.builder()
                .id(propertyId)
                .landlord(authUser)
                .title(propertyRequest.title() != null ? propertyRequest.title() : property.getTitle())
                .description(propertyRequest.description() != null ? propertyRequest.description() : property.getDescription())
                .address(propertyRequest.address() != null ? propertyRequest.address() : property.getAddress())
                .city(propertyRequest.city() != null ? propertyRequest.city() : property.getCity())
                .department(propertyRequest.department() != null ? propertyRequest.department() : property.getDepartment())
                .country(propertyRequest.country() != null ? propertyRequest.country() : property.getCountry())
                .basePricePerNight(propertyRequest.basePricePerNight() != null ? propertyRequest.basePricePerNight() : property.getBasePricePerNight())
                .cleaningFee(propertyRequest.cleaningFee() != null ? propertyRequest.cleaningFee() : property.getCleaningFee())
                .securityDepositAmount(propertyRequest.securityDepositAmount() != null ? propertyRequest.securityDepositAmount() : property.getSecurityDepositAmount())
                .maxGuests(propertyRequest.maxGuests() != null ? propertyRequest.maxGuests() : property.getMaxGuests())
                .bedrooms(propertyRequest.bedrooms() != null ? propertyRequest.bedrooms() : property.getBedrooms())
                .bathrooms(propertyRequest.bathrooms() != null ? propertyRequest.bathrooms() : property.getBathrooms())
                .areaSqm(propertyRequest.areaSqm() != null ? propertyRequest.areaSqm() : property.getAreaSqm())
                .propertyType(propertyRequest.propertyType() != null ? propertyRequest.propertyType() : property.getPropertyType())
                .propertyStatus(propertyRequest.propertyStatus() != null ? propertyRequest.propertyStatus() : property.getPropertyStatus())
                .rules(propertyRequest.rules() != null ? propertyRequest.rules() : property.getRules())
                .photos(property.getPhotos())
                .createdAt(property.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        Property savedProperty = propertyRepository.save(valuesToUpdate);

        return PropertyResponse.fromEntity(savedProperty);
    }

    @Override
    public void deleteProperty(UUID propertyId) {
        AppUser authUser = authProvider.getCurrentUser();

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFound("Property not found"));

        checkPropertyOwnership(property, authUser);

        // Delete photos from Cloudinary before removing the property, to avoid orphaned files
        if (property.getPhotos() != null) {
            property.getPhotos().stream()
                    .filter(photo -> photo.getCloudinaryPublicId() != null)
                    .forEach(photo -> cloudinaryService.deleteFile(photo.getCloudinaryPublicId(), "image"));
        }

        propertyRepository.delete(property);
    }

    //    Private methods
    private void checkPropertyOwnership(Property property, AppUser user) {
        if (!property.getLandlord().getId().equals(user.getId())) {
            throw new NotResourceOwnerException("User is not the owner of the property");
        }
    }

    private PropertyResponse attachPhotosToPropertyByList(Property property, List<AttachPhotoRequest.PhotoEntry> photoEntries) {
        List<PropertyPhoto> photos = photoEntries.stream()
                .filter(Objects::nonNull)
                .filter(entry -> entry.url() != null)
                .map(entry -> PropertyPhoto.builder()
                        .property(property)
                        .url(entry.url())
                        .cloudinaryPublicId(entry.publicId())
                        .build())
                .toList();

        if (!photos.isEmpty()) {
            propertyPhotoRepository.saveAll(photos);
            property.setUpdatedAt(LocalDateTime.now());
            propertyRepository.save(property);
        }

        return PropertyResponse.fromEntity(property);
    }
}
