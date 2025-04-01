package com.hcmus.ecommerce_backend.user.service;

import com.hcmus.ecommerce_backend.user.model.dto.request.CreateUserRequest;
import com.hcmus.ecommerce_backend.user.model.dto.request.UpdateUserRequest;
import com.hcmus.ecommerce_backend.user.model.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(String id);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(String id, UpdateUserRequest request);

    void deleteUser(String id);
}