package com.example.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    @Id
    private String id; // ID này sẽ trùng với ID bên Auth Service

    @Column(nullable = false, unique = true)
    private String email;

    private String fullName;

    private String phoneNumber;

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses;

    @OneToOne(mappedBy = "userProfile", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private Wallet wallet;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
