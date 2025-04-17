package com.hcmus.ecommerce_backend.user.service.impl;

import com.hcmus.ecommerce_backend.common.service.EmailService;
import com.hcmus.ecommerce_backend.user.exception.EmailAlreadyConfirmedException;
import com.hcmus.ecommerce_backend.user.exception.UserAlreadyExistsException;
import com.hcmus.ecommerce_backend.user.exception.UserNotAuthorizedException;
import com.hcmus.ecommerce_backend.user.exception.UserNotFoundException;
import com.hcmus.ecommerce_backend.user.model.dto.request.ChangePasswordRequest;
import com.hcmus.ecommerce_backend.user.model.dto.request.CreateUserRequest;
import com.hcmus.ecommerce_backend.user.model.dto.request.UpdateUserRequest;
import com.hcmus.ecommerce_backend.user.model.dto.response.UserResponse;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.model.mapper.UserMapper;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;
import com.hcmus.ecommerce_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public List<UserResponse> getAllUsers() {
        log.info("UserServiceImpl | getAllUsers | Retrieving all users");
        try {
            List<UserResponse> users = userRepository.findAll().stream()
                    .map(userMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("UserServiceImpl | getAllUsers | Found {} users", users.size());
            return users;
        } catch (DataAccessException e) {
            log.error("UserServiceImpl | getAllUsers | Error retrieving users: {}", e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("UserServiceImpl | getAllUsers | Unexpected error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public UserResponse getUserById(String id) {
        log.info("UserServiceImpl | getUserById | id: {}", id);
        try {
            User user = findUserById(id);
            log.info("UserServiceImpl | getUserById | User found: {}", user.getEmail());
            return userMapper.toResponse(user);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("UserServiceImpl | getUserById | Database error for id {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("UserServiceImpl | getUserById | Unexpected error for id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("UserServiceImpl | createUser | Creating user with email: {}", request.getEmail());
        try {
            checkUserEmailExists(request.getEmail());
            checkUserPhoneNumExists(request.getPhoneNum());

            User user = userMapper.toEntity(request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            User savedUser = userRepository.save(user);
            log.info("UserServiceImpl | createUser | Created user with id: {}", savedUser.getId());

            // Send email confirmation
            String token = savedUser.getConfirmationToken();
            emailService.sendEmailConfirmation(savedUser.getEmail(),
                    savedUser.getFirstName() + ' ' + savedUser.getLastName(), token);
            log.info("UserServiceImpl | createUser | Email confirmation sent to {}", savedUser.getEmail());

            return userMapper.toResponse(savedUser);
        } catch (UserAlreadyExistsException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("UserServiceImpl | createUser | Database error creating user '{}': {}",
                    request.getEmail(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("UserServiceImpl | createUser | Unexpected error creating user '{}': {}",
                    request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public UserResponse updateUser(String id, UpdateUserRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String authenticatedUserId = jwt.getClaimAsString("userId");
            log.info("UserServiceImpl | updateUser | Authenticated user id: {}", authenticatedUserId);

            // Check if the authenticated user ID matches the ID being updated
            if (!authenticatedUserId.equals(id)) {
                log.error(
                        "UserServiceImpl | updateUser | Authenticated user id '{}' does not match the target user id '{}'",
                        authenticatedUserId, id);
                throw new UserNotAuthorizedException(id);
            }
        }

        log.info("UserServiceImpl | updateUser | Updating user with id: {}", id);
        try {
            User user = findUserById(id);

            // Check if email or phone number already exists for another user
            if (!user.getEmail().equals(request.getEmail())) {
                checkUserEmailExists(request.getEmail());
            }
            if (!user.getPhoneNum().equals(request.getPhoneNum())) {
                checkUserPhoneNumExists(request.getPhoneNum());
            }

            userMapper.updateEntity(request, user);
            User updatedUser = userRepository.save(user);
            log.info("UserServiceImpl | updateUser | Updated user with id: {}", updatedUser.getId());
            return userMapper.toResponse(updatedUser);
        } catch (UserNotFoundException | UserAlreadyExistsException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("UserServiceImpl | updateUser | Database error updating user with id '{}': {}",
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("UserServiceImpl | updateUser | Unexpected error updating user with id '{}': {}",
                    id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        log.info("UserServiceImpl | deleteUser | Deleting user with id: {}", id);
        try {
            if (!doesUserExistById(id)) {
                throw new UserNotFoundException("User not found with id: " + id);
            }

            userRepository.deleteById(id);
            log.info("UserServiceImpl | deleteUser | Deleted user with id: {}", id);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("UserServiceImpl | deleteUser | Database error deleting user with id '{}': {}",
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("UserServiceImpl | deleteUser | Unexpected error deleting user with id '{}': {}",
                    id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        log.info("UserServiceImpl | changePassword | Changing password for user with id: {}", userId);

        try {
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new IllegalArgumentException("New password and confirm password do not match");
            }

            User user = findUserById(userId);

            // Check if the current password is correct
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                log.error("UserServiceImpl | changePassword | Current password is incorrect for user id: {}", userId);
                throw new IllegalArgumentException("Current password is incorrect");
            }

            // Set the new password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));

            // Increment token version to invalidate all existing tokens
            user.setTokenVersion(user.getTokenVersion() + 1);

            userRepository.save(user);
            log.info("UserServiceImpl | changePassword | Password changed successfully for user with id: {}", userId);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("UserServiceImpl | changePassword | Validation error: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            log.error("UserServiceImpl | changePassword | Database error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("UserServiceImpl | changePassword | Unexpected error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean confirmEmail(String token) {
        log.info("Confirming email with token: {}", token);

        User user = userRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid confirmation token"));

        if (LocalDateTime.now().isAfter(user.getConfirmationTokenExpiry())) {
            throw new RuntimeException("Confirmation token expired");
        }

        user.setEnabled(true);
        user.setConfirmationToken(null);
        user.setConfirmationTokenExpiry(null);
        User savedUser = userRepository.save(user);

        log.info("Email confirmed successfully for user: {}", savedUser.getId());
        return true;
    }

    @Override
    @Transactional
    public void resendConfirmationEmail(String email) {
        log.info("UserServiceImpl | resendConfirmationEmail | Resending confirmation email to: {}", email);

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("UserServiceImpl | resendConfirmationEmail | User not found with email: {}", email);
                        return new UserNotFoundException("User not found with email: " + email);
                    });

            // Skip if user is already enabled
            if (user.isEnabled()) {
                log.info("UserServiceImpl | resendConfirmationEmail | Email already confirmed for user: {}", email);
                throw new EmailAlreadyConfirmedException();
            }

            // Generate new confirmation token and update expiry
            String newToken = java.util.UUID.randomUUID().toString();
            user.setConfirmationToken(newToken);
            user.setConfirmationTokenExpiry(LocalDateTime.now().plusHours(24));

            User savedUser = userRepository.save(user);
            log.info("UserServiceImpl | resendConfirmationEmail | Token refreshed for user: {}", savedUser.getId());

            // Send email confirmation
            emailService.sendEmailConfirmation(
                    savedUser.getEmail(),
                    savedUser.getFirstName() + ' ' + savedUser.getLastName(),
                    newToken);
            log.info("UserServiceImpl | resendConfirmationEmail | New email confirmation sent to {}",
                    savedUser.getEmail());

        } catch (UserNotFoundException e) {
            throw e;
        } catch (EmailAlreadyConfirmedException e) {
            log.warn("UserServiceImpl | resendConfirmationEmail | {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            log.error("UserServiceImpl | resendConfirmationEmail | Database error for email '{}': {}",
                    email, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("UserServiceImpl | resendConfirmationEmail | Unexpected error for email '{}': {}",
                    email, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private User findUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("UserServiceImpl | findUserById | User not found with id: {}", id);
                    return new UserNotFoundException("User not found with id: " + id);
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private boolean doesUserExistById(String id) {
        return userRepository.existsById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private void checkUserEmailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            log.error("UserServiceImpl | checkUserEmailExists | User already exists with email: {}", email);
            throw new UserAlreadyExistsException(email, true);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private void checkUserPhoneNumExists(String phoneNum) {
        if (userRepository.existsByPhoneNum(phoneNum)) {
            log.error("UserServiceImpl | checkUserPhoneNumExists | User already exists with phone number: {}",
                    phoneNum);
            throw new UserAlreadyExistsException(phoneNum, false);
        }
    }
}