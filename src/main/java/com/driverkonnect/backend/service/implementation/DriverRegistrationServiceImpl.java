package com.driverkonnect.backend.service.implementation;

import com.driverkonnect.backend.dto.request.driver.DriverPasswordUpdateRequestDto;
import com.driverkonnect.backend.dto.request.driver.DriverRegisterRequestDto;
import com.driverkonnect.backend.dto.response.driver.DriverApplicationResponseDto;
import com.driverkonnect.backend.entity.DriverApplication;
import com.driverkonnect.backend.entity.DriverDocument;
import com.driverkonnect.backend.entity.User;
import com.driverkonnect.backend.entity.UserRole;
import com.driverkonnect.backend.enums.ApplicationStatus;
import com.driverkonnect.backend.enums.DocumentType;
import com.driverkonnect.backend.exception.CustomException;
import com.driverkonnect.backend.repository.DriverApplicationRepository;
import com.driverkonnect.backend.repository.DriverDocumentRepository;
import com.driverkonnect.backend.repository.UserRepository;
import com.driverkonnect.backend.repository.UserRoleRepository;
import com.driverkonnect.backend.service.DriverRegistrationService;
import com.driverkonnect.backend.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverRegistrationServiceImpl implements DriverRegistrationService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png");
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final DriverApplicationRepository driverApplicationRepository;
    private final DriverDocumentRepository driverDocumentRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public DriverApplicationResponseDto register(DriverRegisterRequestDto request) {
        String email = request.getEmail().strip().toLowerCase();
        log.debug("Starting driver registration for email: {}", email);

        if (userRepository.existsByEmail(email)) {
            log.warn("Registration attempt with already-registered email: {}", email);
            throw new CustomException("An account with this email already exists", 409);
        }

        String nic = request.getNicNumber().strip().toUpperCase();
        if (driverApplicationRepository.existsByNicNumber(nic)) {
            log.warn("Registration attempt with already-registered NIC: {}", nic);
            throw new CustomException("An application with this NIC number already exists", 409);
        }

        UserRole driverRole = userRoleRepository.findByRole("DRIVER")
                .orElseThrow(() -> {
                    log.error("DRIVER role not found in database — check seed data in V1 migration");
                    return new CustomException("DRIVER role not configured", 500);
                });

        User user = new User();
        user.setFirstName(request.getFirstName().strip());
        user.setLastName(request.getLastName().strip());
        user.setUsername(email);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserRole(driverRole);
        user.setIsActive(true);
        user.setIsFirstLogin(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);
        log.debug("User account created with ID: {}", user.getId());

        DriverApplication application = new DriverApplication();
        application.setUser(user);
        application.setFullName(request.getFullName().strip());
        application.setDateOfBirth(request.getDateOfBirth());
        application.setPhone(request.getPhone().strip());
        application.setWhatsapp(request.getWhatsapp() != null ? request.getWhatsapp().strip() : null);
        application.setNicNumber(nic);
        application.setLanguagesSpoken(String.join(",", request.getLanguagesSpoken()));
        application.setYearsOfExperience(request.getYearsOfExperience());
        application.setAvailability(request.getAvailability());
        application.setStatus(ApplicationStatus.DRAFT);
        application.setPrivacyPolicyAccepted(request.getPrivacyPolicyAccepted());
        application.setPrivacyPolicyAcceptedAt(LocalDateTime.now());
        application.setPrivacyPolicyVersion(
                request.getPrivacyPolicyVersion() != null ? request.getPrivacyPolicyVersion() : "1.0");
        application.setCreatedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        application = driverApplicationRepository.save(application);

        String appNumber = String.format("DK-%d-%04d", LocalDate.now().getYear(), application.getId());
        application.setApplicationNumber(appNumber);
        application = driverApplicationRepository.save(application);
        log.debug("Driver application created: {} for user ID: {}", appNumber, user.getId());

        return toResponseDto(application);
    }

    @Override
    @Transactional
    public DriverApplicationResponseDto uploadDocuments(Long applicationId,
                                                         MultipartFile licenceFront,
                                                         MultipartFile licenceBack,
                                                         MultipartFile policeClearance) {
        log.debug("Processing document upload for application ID: {}", applicationId);

        DriverApplication application = driverApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException("Application not found", 404));

        String authenticatedEmail = AuthUtil.getAuthenticatedUser().getUsername();
        if (!application.getUser().getEmail().equals(authenticatedEmail)) {
            log.warn("Unauthorized document upload attempt on application ID: {} by: {}",
                    applicationId, authenticatedEmail);
            throw new CustomException("Access denied", 403);
        }

        if (application.getStatus() != ApplicationStatus.DRAFT) {
            log.warn("Document upload attempted on non-DRAFT application ID: {} (status: {})",
                    applicationId, application.getStatus());
            throw new CustomException("Documents have already been submitted for this application", 400);
        }

        saveDocument(application, licenceFront, DocumentType.LICENCE_FRONT);
        saveDocument(application, licenceBack, DocumentType.LICENCE_BACK);
        saveDocument(application, policeClearance, DocumentType.POLICE_CLEARANCE);

        application.setStatus(ApplicationStatus.PENDING);
        application.setSubmittedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        application = driverApplicationRepository.save(application);
        log.debug("Application ID: {} status updated to PENDING", applicationId);

        return toResponseDto(application);
    }

    @Override
    @Transactional
    public void updatePassword(DriverPasswordUpdateRequestDto request) {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        log.debug("Processing password update for user: {}", email);

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new CustomException("New password and confirm password do not match", 400);
        }

        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new CustomException("User not found", 404));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password update failed — incorrect current password for user: {}", email);
            throw new CustomException("Current password is incorrect", 400);
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new CustomException("New password must be different from the current password", 400);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        refreshTokenService.deleteKeyByUsername(user.getUsername());
        log.debug("Password updated and session invalidated for user: {}", email);
    }

    @Override
    public DriverApplicationResponseDto getMyApplication() {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        log.debug("Fetching application for authenticated user: {}", email);

        DriverApplication application = driverApplicationRepository.findByUser_Email(email)
                .orElseThrow(() -> new CustomException("No application found for this account", 404));

        return toResponseDto(application);
    }

    private void saveDocument(DriverApplication application, MultipartFile file, DocumentType type) {
        validateFile(file, type);

        String extension = getExtension(file.getOriginalFilename());
        String fileName = type.name().toLowerCase() + "_" + System.currentTimeMillis() + extension;
        Path uploadPath = Paths.get(uploadDir, "driver-documents", application.getId().toString())
                .toAbsolutePath();

        log.debug("Saving {} document to path: {}", type.name(), uploadPath);

        try {
            Files.createDirectories(uploadPath);
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to store {} file for application ID: {} — path: {} — reason: {}",
                    type.name(), application.getId(), uploadPath.resolve(fileName), e.getMessage(), e);
            throw new CustomException("Failed to store file: " + type.name(), 500);
        }

        DriverDocument doc = new DriverDocument();
        doc.setApplication(application);
        doc.setDocumentType(type);
        doc.setFileName(fileName);
        doc.setFilePath(uploadPath.resolve(fileName).toString());
        doc.setUploadedAt(LocalDateTime.now());
        driverDocumentRepository.save(doc);
        log.debug("Successfully saved {} document: {}", type.name(), fileName);
    }

    private void validateFile(MultipartFile file, DocumentType type) {
        if (file == null || file.isEmpty()) {
            log.warn("Empty file received for document type: {}", type.name());
            throw new CustomException(type.name() + " file cannot be empty", 400);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("File too large for document type: {} — size: {} bytes", type.name(), file.getSize());
            throw new CustomException(type.name() + " file must not exceed 10MB", 400);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            log.warn("Invalid content type for document type: {} — received: {}", type.name(), contentType);
            throw new CustomException(type.name() + ": only JPG and PNG files are allowed", 400);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }

    private DriverApplicationResponseDto toResponseDto(DriverApplication application) {
        DriverApplicationResponseDto dto = new DriverApplicationResponseDto();
        dto.setId(application.getId());
        dto.setApplicationNumber(application.getApplicationNumber());
        dto.setStatus(application.getStatus().name());
        dto.setFullName(application.getFullName());
        dto.setEmail(application.getUser().getEmail());
        dto.setPrivacyPolicyAccepted(application.getPrivacyPolicyAccepted());
        dto.setPrivacyPolicyVersion(application.getPrivacyPolicyVersion());
        dto.setSubmittedAt(application.getSubmittedAt());
        dto.setCreatedAt(application.getCreatedAt());
        return dto;
    }
}
