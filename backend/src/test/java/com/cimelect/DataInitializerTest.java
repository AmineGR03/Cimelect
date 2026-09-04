package com.cimelect;

import com.cimelect.repository.CustomerRepository;
import com.cimelect.repository.OperationRepository;
import com.cimelect.repository.ShipmentRepository;
import com.cimelect.repository.SupplierRepository;
import com.cimelect.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DataInitializerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Test
    void demoAccountsAndBusinessSeedDataAreCreated() {
        assertThat(userRepository.findByEmail("admin@cimelect.local")).isPresent();
        assertThat(userRepository.findByEmail("responsable@cimelect.local")).isPresent();
        assertThat(userRepository.findByEmail("agent@cimelect.local")).isPresent();

        assertThat(supplierRepository.count()).isGreaterThan(0);
        assertThat(customerRepository.count()).isGreaterThan(0);
        assertThat(operationRepository.count()).isGreaterThan(0);
        assertThat(shipmentRepository.count()).isGreaterThan(0);
    }
}
