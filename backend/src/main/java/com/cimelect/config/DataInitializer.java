package com.cimelect.config;

import com.cimelect.entity.RequiredDocument;
import com.cimelect.entity.User;
import com.cimelect.enums.DocumentType;
import com.cimelect.enums.OperationType;
import com.cimelect.enums.Role;
import com.cimelect.repository.RequiredDocumentRepository;
import com.cimelect.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RequiredDocumentRepository requiredDocumentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public DataInitializer(
            UserRepository userRepository,
            RequiredDocumentRepository requiredDocumentRepository,
            PasswordEncoder passwordEncoder,
            AppProperties appProperties
    ) {
        this.userRepository = userRepository;
        this.requiredDocumentRepository = requiredDocumentRepository;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    @Override
    public void run(String... args) {
        ensureBootstrapAdmin();
        seedRequired(OperationType.IMPORT, DocumentType.FACTURE);
        seedRequired(OperationType.IMPORT, DocumentType.PACKING_LIST);
        seedRequired(OperationType.IMPORT, DocumentType.TRANSPORT);
        seedRequired(OperationType.EXPORT, DocumentType.FACTURE);
        seedRequired(OperationType.EXPORT, DocumentType.TRANSPORT);
    }

    private void ensureBootstrapAdmin() {
        String email = appProperties.bootstrap().admin().email();
        String password = appProperties.bootstrap().admin().password();
        User admin = userRepository.findByEmail(email).orElseGet(() -> User.builder()
                .firstName("Admin")
                .lastName("Cimelect")
                .email(email)
                .role(Role.ADMINISTRATEUR)
                .enabled(true)
                .build());

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(password));
        }
        admin.setRole(Role.ADMINISTRATEUR);
        admin.setEnabled(true);
        userRepository.save(admin);
    }

    private void seedRequired(OperationType type, DocumentType documentType) {
        requiredDocumentRepository.findByOperationTypeAndDocumentType(type, documentType)
                .orElseGet(() -> requiredDocumentRepository.save(RequiredDocument.builder()
                        .operationType(type)
                        .documentType(documentType)
                        .required(true)
                        .build()));
    }
}
