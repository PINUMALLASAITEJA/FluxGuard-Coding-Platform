package com.codingplatform.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codingplatform.dto.RegisterRequest;
import com.codingplatform.model.UserAccount;
import com.codingplatform.repository.UserAccountRepository;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountServiceImpl(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserAccount registerUser(RegisterRequest registerRequest) {
        if (userAccountRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email already registered.");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setFullName(registerRequest.getFullName());
        userAccount.setEmail(registerRequest.getEmail());
        userAccount.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userAccount.setCreatedAt(LocalDateTime.now());

        return userAccountRepository.save(userAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public UserAccount findByEmail(String email) {
        return userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }
}