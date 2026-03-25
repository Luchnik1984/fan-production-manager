package com.fanproduction.services.impl;

import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.Role;
import com.fanproduction.core.enums.UserStatus;
import com.fanproduction.repositories.UserRepository;
import com.fanproduction.services.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void testDatabaseConnection() {
        System.out.println("=== Testing database connection ===");

        long count = userRepository.count();
        System.out.println("Current users in database: " + count);

        if (count == 0) {
            // Создаём тестового ADMIN
            UserEntity adminUser = new UserEntity();
            adminUser.setEmail("admin@test.com");
            adminUser.setPassword(passwordEncoder.encode("admin"));
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setRole(Role.ADMIN);
            adminUser.setStatus(UserStatus.ACTIVE);
            userRepository.save(adminUser);
            System.out.println("✅ Test admin user created with id: " + adminUser.getId());

            // Создаём тестового ENGINEER
            UserEntity engineerUser = new UserEntity();
            engineerUser.setEmail("engineer@test.com");
            engineerUser.setPassword(passwordEncoder.encode("engineer"));
            engineerUser.setFirstName("Test");
            engineerUser.setLastName("Engineer");
            engineerUser.setRole(Role.ENGINEER);
            engineerUser.setStatus(UserStatus.PENDING);
            userRepository.save(engineerUser);
            System.out.println("✅ Test engineer user created with id: " + engineerUser.getId());

            // Создаём тестового MANAGER
            UserEntity managerUser = new UserEntity();
            managerUser.setEmail("manager@test.com");
            managerUser.setPassword(passwordEncoder.encode("manager"));
            managerUser.setFirstName("Test");
            managerUser.setLastName("Manager");
            managerUser.setRole(Role.MANAGER);
            managerUser.setStatus(UserStatus.PENDING);
            userRepository.save(managerUser);
            System.out.println("✅ Test manager user created with id: " + managerUser.getId());
        }

        System.out.println("=== Test completed ===");
    }
}
