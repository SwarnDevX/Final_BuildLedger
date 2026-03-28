package com.buildledger.dto.response;

import com.buildledger.enums.Role;
import com.buildledger.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserResponseDTO {
    private Long userId;
    private String username;
    private String name;
    private Role role;
    private String email;
    private String phone;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
