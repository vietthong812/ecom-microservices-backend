package com.example.auth_service.service;

import com.example.auth_service.entity.User;
import com.example.auth_service.entity.UserPrincipal;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(
            String email
    ) {
        User user =
                userRepository
                        .findUserByEmail(email)
                        .orElseThrow();
        return new UserPrincipal(user);
    }
}
