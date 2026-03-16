package com.fanproduction.services.impl;

import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.Role;
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
            UserEntity testUser = new UserEntity();
            testUser.setEmail("test@example.com");
            testUser.setPassword("password123"); // В реальности нужно шифровать!
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser.setRole(Role.ENGINEER);

            userRepository.save(testUser);
            System.out.println("Test user created with id: " + testUser.getId());
        }

        System.out.println("=== Test completed ===");
    }
}
