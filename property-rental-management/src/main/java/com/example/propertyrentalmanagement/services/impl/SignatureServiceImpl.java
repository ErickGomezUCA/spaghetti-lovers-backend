package com.example.propertyrentalmanagement.services.impl;

import com.example.propertyrentalmanagement.entitites.AppUser;
import com.example.propertyrentalmanagement.entitites.Signature;
import com.example.propertyrentalmanagement.repositories.SignatureRepository;
import com.example.propertyrentalmanagement.services.AppUserService;
import com.example.propertyrentalmanagement.services.SignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignatureServiceImpl implements SignatureService {
    private final SignatureRepository signatureRepository;
    private final AppUserService appUserService; // TODO: Ask if i need to check user by service or by repo

    @Override
    public Signature createSignature(UUID userId, UUID contractId) {
        try {
            AppUser user = appUserService.getUserById(userId).toEntity();

            String hashContent = userId.toString() + contractId.toString() + LocalDateTime.now();
            String hash = generateHash(hashContent);
            Signature signature = Signature.builder()
                    .hash(hash)
                    .signedTimestamp(LocalDateTime.now())
                    .user(user)
                    .build();
            return signatureRepository.save(signature); // TODO: Convert into DTO?

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating signature hash", e); // TODO: Set custom exception
        }
    }

    private String generateHash(String content) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(content.getBytes(StandardCharsets.UTF_8));

        // Convert byte array into a hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : encodedHash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
