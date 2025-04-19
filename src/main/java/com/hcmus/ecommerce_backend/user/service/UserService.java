package com.hcmus.ecommerce_backend.user.service;

import com.hcmus.ecommerce_backend.user.model.dto.request.ChangePasswordRequest;
import com.hcmus.ecommerce_backend.user.model.dto.request.CreateUserRequest;
import com.hcmus.ecommerce_backend.user.model.dto.request.ResetPasswordRequest;
import com.hcmus.ecommerce_backend.user.model.dto.request.UpdateUserRequest;
import com.hcmus.ecommerce_backend.user.model.dto.response.UserResponse;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse getUserById(String id);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(String id, UpdateUserRequest request);

    void deleteUser(String id);
    
    void changePassword(String userId, ChangePasswordRequest request);

    public boolean confirmEmail(String token);

    void resendConfirmationEmail(String email);

    void sendResetPasswordEmail(String email);

    void validateResetToken(String token);

    void resetPassword(ResetPasswordRequest request);
}