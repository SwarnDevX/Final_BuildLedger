package com.buildledger.service;

import com.buildledger.dto.request.LoginRequestDTO;
import com.buildledger.dto.response.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
}
