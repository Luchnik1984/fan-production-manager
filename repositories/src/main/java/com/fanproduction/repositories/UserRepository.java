package com.fanproduction.repositories;

import com.fanproduction.core.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
