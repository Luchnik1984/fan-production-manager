package com.fanproduction.repositories;

import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Найти пользователя по email
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Проверить, существует ли пользователь с таким email
     */
    boolean existsByEmail(String email);

    /**
     * Поиск по статусу
     */
    List<UserEntity> findByStatus(UserStatus status);
}
