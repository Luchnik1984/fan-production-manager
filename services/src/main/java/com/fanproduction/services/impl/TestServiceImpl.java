package com.fanproduction.services.impl;

import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.Role;
import com.fanproduction.core.enums.UserStatus;
import com.fanproduction.repositories.UserRepository;
import com.fanproduction.services.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void testDatabaseConnection() {
        System.out.println("=== Testing database connection ===");

        // Проверяем, есть ли пользователи
        long count = userRepository.count();
        System.out.println("Current users in database: " + count);

        /// Если нет ни одного пользователя, создадим тестового
        if (count == 0) {
            // Создаём тестового ADMIN (будет ACTIVE)
            UserEntity adminUser = new UserEntity();
            adminUser.setEmail("admin@test.com");
            adminUser.setPassword("admin");
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setRole(Role.ADMIN);
            adminUser.setStatus(UserStatus.ACTIVE);  // ADMIN сразу активен

            userRepository.save(adminUser);
            System.out.println("✅ Test admin user created with id: " + adminUser.getId() + " (ACTIVE)");

            // Создаём тестового ENGINEER (будет PENDING)
            UserEntity engineerUser = new UserEntity();
            engineerUser.setEmail("engineer@test.com");
            engineerUser.setPassword("engineer");
            engineerUser.setFirstName("Test");
            engineerUser.setLastName("Engineer");
            engineerUser.setRole(Role.ENGINEER);
            engineerUser.setStatus(UserStatus.PENDING);  // ожидает подтверждения

            userRepository.save(engineerUser);
            System.out.println("✅ Test engineer user created with id: " + engineerUser.getId() + " (PENDING)");

            // Создаём тестового MANAGER (будет PENDING)
            UserEntity managerUser = new UserEntity();
            managerUser.setEmail("manager@test.com");
            managerUser.setPassword("manager");
            managerUser.setFirstName("Test");
            managerUser.setLastName("Manager");
            managerUser.setRole(Role.MANAGER);
            managerUser.setStatus(UserStatus.PENDING);

            userRepository.save(managerUser);
            System.out.println("✅ Test manager user created with id: " + managerUser.getId() + " (PENDING)");

        }
        System.out.println("=== Test completed ===");
    }
}
