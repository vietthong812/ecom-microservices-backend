package com.example.user_service.entity;
import com.example.user_service.entity.UserProfile;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    private String id; // Dùng chung ID với UserProfile

    private Double balance = 0.0;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private UserProfile userProfile;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    private List<WalletTransaction> transactions;
}