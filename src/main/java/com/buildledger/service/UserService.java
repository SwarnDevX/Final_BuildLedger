package com.buildledger.service;

import com.buildledger.dto.request.CreateUserRequestDTO;
import com.buildledger.dto.request.UpdateUserRequestDTO;
import com.buildledger.dto.response.UserResponseDTO;
import com.buildledger.enums.Role;
import java.util.List;

public interface UserService {
    UserResponseDTO createUser(CreateUserRequestDTO request);
    UserResponseDTO getUserById(Long userId);
    UserResponseDTO getUserByUsername(String username);
    List<UserResponseDTO> getAllUsers();
    List<UserResponseDTO> getUsersByRole(Role role);
    UserResponseDTO updateUser(Long userId, UpdateUserRequestDTO request);
    void deleteUser(Long userId);
}
