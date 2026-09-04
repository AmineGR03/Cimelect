package com.cimelect.config;

import com.cimelect.entity.*;
import com.cimelect.enums.*;
import com.cimelect.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RequiredDocumentRepository requiredDocumentRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OperationRepository operationRepository;
    private final ShipmentRepository shipmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public DataInitializer(
            UserRepository userRepository,
            RequiredDocumentRepository requiredDocumentRepository,
            SupplierRepository supplierRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            OperationRepository operationRepository,
            ShipmentRepository shipmentRepository,
            PasswordEncoder passwordEncoder,
            AppProperties appProperties
    ) {
        this.userRepository = userRepository;
        this.requiredDocumentRepository = requiredDocumentRepository;
        this.supplierRepository = supplierRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.operationRepository = operationRepository;
        this.shipmentRepository = shipmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    @Override
    @Transactional
    public void run(String... args) {
        ensureDemoUsers();
        seedRequired(OperationType.IMPORT, DocumentType.FACTURE);
        seedRequired(OperationType.IMPORT, DocumentType.PACKING_LIST);
        seedRequired(OperationType.IMPORT, DocumentType.TRANSPORT);
        seedRequired(OperationType.EXPORT, DocumentType.FACTURE);
        seedRequired(OperationType.EXPORT, DocumentType.TRANSPORT);
        seedDemoBusinessData();
    }

    private void ensureDemoUsers() {
        ensureUser(
                appProperties.bootstrap().admin().email(),
                appProperties.bootstrap().admin().password(),
                "Admin",
                "Cimelect",
                Role.ADMINISTRATEUR
        );
        ensureUser("responsable@cimelect.local", "Responsable123!", "Responsable", "Cimelect", Role.RESPONSABLE);
        ensureUser("agent@cimelect.local", "Agent123!", "Agent", "Cimelect", Role.AGENT_IMPORT_EXPORT);
    }

    private void ensureUser(String email, String password, String firstName, String lastName, Role role) {
        User user = userRepository.findByEmail(email).orElseGet(() -> User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .role(role)
                .enabled(true)
                .build());

        if (!passwordEncoder.matches(password, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(password));
        }
        user.setRole(role);
        user.setEnabled(true);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        userRepository.save(user);
    }

    private void seedRequired(OperationType type, DocumentType documentType) {
        requiredDocumentRepository.findByOperationTypeAndDocumentType(type, documentType)
                .orElseGet(() -> requiredDocumentRepository.save(RequiredDocument.builder()
                        .operationType(type)
                        .documentType(documentType)
                        .required(true)
                        .build()));
    }

    private void seedDemoBusinessData() {
        if (supplierRepository.count() == 0) {
            supplierRepository.save(Supplier.builder().companyName("Nordic Trade Co.").country("Danemark").contactName("Sofia Hansen").email("sofia@nordictrade.dk").phone("+45 12 45 67 89").build());
            supplierRepository.save(Supplier.builder().companyName("AsiaSource Logistics").country("Chine").contactName("Yin Chen").email("yin@asiasource.cn").phone("+86 21 5555 1010").build());
        }

        if (customerRepository.count() == 0) {
            customerRepository.save(Customer.builder().companyName("Maison Étoile").country("France").contactName("Luc Martin").email("luc@maison-etoile.fr").phone("+33 1 40 00 10 00").build());
            customerRepository.save(Customer.builder().companyName("Atlas Retail Group").country("Allemagne").contactName("Mila Weber").email("mila@atlas-retail.de").phone("+49 30 555 1122").build());
        }

        if (productRepository.count() == 0) {
            productRepository.save(Product.builder().sku("ELE-2200").name("Électroménager Premium").description("Cuisinière compacte").unit("unité").build());
            productRepository.save(Product.builder().sku("LUX-880").name("Textile haut de gamme").description("Collection hiver").unit("colis").build());
            productRepository.save(Product.builder().sku("TEC-410").name("Composant électronique").description("Module de contrôle").unit("boîte").build());
        }

        if (operationRepository.count() > 0) {
            return;
        }

        User admin = userRepository.findByEmail(appProperties.bootstrap().admin().email())
                .orElseThrow(() -> new IllegalStateException("Admin bootstrap introuvable"));
        Supplier supplier = supplierRepository.findByArchivedFalse().stream().findFirst().orElseThrow();
        Customer customer = customerRepository.findByArchivedFalse().stream().findFirst().orElseThrow();
        Product productA = productRepository.findByActiveTrue().stream().findFirst().orElseThrow();
        Product productB = productRepository.findByActiveTrue().stream().skip(1).findFirst().orElseThrow();

        Operation importOperation = Operation.builder()
                .reference("IMP-2026-0001")
                .type(OperationType.IMPORT)
                .supplier(supplier)
                .destination("Le Havre")
                .orderDate(LocalDate.now().minusDays(18))
                .expectedDate(LocalDate.now().minusDays(3))
                .actualDate(LocalDate.now().minusDays(1))
                .carrier("Maersk")
                .plannedCost(new BigDecimal("12500.00"))
                .actualCost(new BigDecimal("13340.00"))
                .status(OperationStatus.EN_TRANSIT)
                .createdBy(admin)
                .build();
        importOperation.addLine(OperationLine.builder().product(productA).quantity(new BigDecimal("120")).unitPrice(new BigDecimal("52.75")).build());
        importOperation.addLine(OperationLine.builder().product(productB).quantity(new BigDecimal("85")).unitPrice(new BigDecimal("41.30")).build());
        operationRepository.save(importOperation);

        Shipment importShipment = Shipment.builder()
                .operation(importOperation)
                .carrier("Maersk")
                .departureDate(LocalDate.now().minusDays(10))
                .expectedArrivalDate(LocalDate.now().minusDays(4))
                .actualArrivalDate(LocalDate.now().minusDays(1))
                .status(ShipmentStatus.EN_TRANSIT)
                .aiAnalysisTriggered(true)
                .build();
        shipmentRepository.save(importShipment);

        Operation exportOperation = Operation.builder()
                .reference("EXP-2026-0002")
                .type(OperationType.EXPORT)
                .customer(customer)
                .destination("Hambourg")
                .orderDate(LocalDate.now().minusDays(12))
                .expectedDate(LocalDate.now().plusDays(4))
                .carrier("DHL")
                .plannedCost(new BigDecimal("9800.00"))
                .actualCost(new BigDecimal("10150.00"))
                .status(OperationStatus.PREPARATION)
                .createdBy(admin)
                .build();
        exportOperation.addLine(OperationLine.builder().product(productA).quantity(new BigDecimal("60")).unitPrice(new BigDecimal("68.00")).build());
        operationRepository.save(exportOperation);

        shipmentRepository.save(Shipment.builder()
                .operation(exportOperation)
                .carrier("DHL")
                .departureDate(LocalDate.now().minusDays(2))
                .expectedArrivalDate(LocalDate.now().plusDays(3))
                .status(ShipmentStatus.EN_PREPARATION)
                .build());
    }
}
