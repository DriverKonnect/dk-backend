package com.driverkonnect.backend.service.implementation;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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


    @Override
    @Transactional
    public DriverApplicationResponseDto register(DriverRegisterRequestDto request) {
        String email = request.getEmail().strip().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new CustomException("An account with this email already exists", 409);
        }

        if (driverApplicationRepository.existsByNicNumber(request.getNicNumber().strip().toUpperCase())) {
            throw new CustomException("An application with this NIC number already exists", 409);
        }

        UserRole driverRole = userRoleRepository.findByRole("DRIVER")
                .orElseThrow(() -> new CustomException("DRIVER role not configured", 500));

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

        DriverApplication application = new DriverApplication();
        application.setUser(user);
        application.setFullName(request.getFullName().strip());
        application.setDateOfBirth(request.getDateOfBirth());
        application.setPhone(request.getPhone().strip());
        application.setWhatsapp(request.getWhatsapp() != null ? request.getWhatsapp().strip() : null);
        application.setNicNumber(request.getNicNumber().strip().toUpperCase());
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

        return toResponseDto(application);
    }

    @Override
    @Transactional
    public DriverApplicationResponseDto uploadDocuments(Long applicationId,
                                                         MultipartFile licenceFront,
                                                         MultipartFile licenceBack,
                                                         MultipartFile policeClearance) {
        DriverApplication application = driverApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException("Application not found", 404));

        String authenticatedEmail = AuthUtil.getAuthenticatedUser().getUsername();
        if (!application.getUser().getEmail().equals(authenticatedEmail)) {
            throw new CustomException("Access denied", 403);
        }

        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new CustomException("Documents have already been submitted for this application", 400);
        }

        saveDocument(application, licenceFront, DocumentType.LICENCE_FRONT);
        saveDocument(application, licenceBack, DocumentType.LICENCE_BACK);
        saveDocument(application, policeClearance, DocumentType.POLICE_CLEARANCE);

        application.setStatus(ApplicationStatus.PENDING);
        application.setSubmittedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        application = driverApplicationRepository.save(application);

        return toResponseDto(application);
    }

    @Override
    public DriverApplicationResponseDto getMyApplication() {
        String email = AuthUtil.getAuthenticatedUser().getUsername();
        DriverApplication application = driverApplicationRepository.findByUser_Email(email)
                .orElseThrow(() -> new CustomException("No application found for this account", 404));
        return toResponseDto(application);
    }

    private void saveDocument(DriverApplication application, MultipartFile file, DocumentType type) {
        validateFile(file, type);

        String extension = getExtension(file.getOriginalFilename());
        String fileName = type.name().toLowerCase() + "_" + System.currentTimeMillis() + extension;
        Path uploadPath = Paths.get(uploadDir, "driver-documents", application.getId().toString());

        try {
            Files.createDirectories(uploadPath);
            file.transferTo(uploadPath.resolve(fileName).toFile());
        } catch (IOException e) {
            throw new CustomException("Failed to store file: " + type.name(), 500);
        }

        DriverDocument doc = new DriverDocument();
        doc.setApplication(application);
        doc.setDocumentType(type);
        doc.setFileName(fileName);
        doc.setFilePath(uploadPath.resolve(fileName).toString());
        doc.setUploadedAt(LocalDateTime.now());
        driverDocumentRepository.save(doc);
    }

    private void validateFile(MultipartFile file, DocumentType type) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(type.name() + " file cannot be empty", 400);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(type.name() + " file must not exceed 10MB", 400);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
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
