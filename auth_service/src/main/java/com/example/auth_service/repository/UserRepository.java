package com.example.auth_service.repository;

import com.example.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, String> {

    Optional<User> findUserByEmail(String email);

    boolean existsByEmail(String email);

    void deleteById(String aLong);


}