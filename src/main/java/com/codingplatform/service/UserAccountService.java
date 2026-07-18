package com.codingplatform.service;

import com.codingplatform.dto.RegisterRequest;
import com.codingplatform.model.UserAccount;

public interface UserAccountService {

    UserAccount registerUser(RegisterRequest registerRequest);

    UserAccount findByEmail(String email);

}