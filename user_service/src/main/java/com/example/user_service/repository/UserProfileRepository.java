package com.example.user_service.repository;

import com.example.user_service.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    // Tìm hồ sơ theo email (hữu ích để kiểm tra thông tin)
    Optional<UserProfile> findByEmail(String email);

    @Override
    Optional<UserProfile> findById(String aLong);
}