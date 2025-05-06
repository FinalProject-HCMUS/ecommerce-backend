package com.hcmus.ecommerce_backend.user.service.impl;

import com.hcmus.ecommerce_backend.common.service.EmailService;
import com.hcmus.ecommerce_backend.user.exception.*;
import com.hcmus.ecommerce_backend.user.model.dto.request.*;
import com.hcmus.ecommerce_backend.user.model.dto.response.UserResponse;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.model.entity.VerificationToken;
import com.hcmus.ecommerce_backend.user.model.mapper.UserMapper;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;
import com.hcmus.ecommerce_backend.user.repository.VerificationTokenRepository;
import com.hcmus.ecommerce_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("UserServiceImpl | getAllUsers | Retrieving users with pagination - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        try {
            Page<User> userPage = userRepository.findAll(pageable);
            Page<UserResponse> userResponsePage = userPage.map(userMapper::toResponse);

            log.info("UserServiceImpl | getAllUsers | Found {} users on page {} of {}",
                    userResponsePage.getNumberOfElements(),
                    userResponsePage.getNumber() + 1,
                    userResponsePage.getTotalPages());

            return userResponsePage;
        } catch (DataAccessException e) {
            log.error("UserServiceImpl | getAllUsers | Database error retrieving paginated users: {}", e.getMessage(),
                    e);
            return Page.empty(pageable);
        } catch (Exception e) {
            log.error("UserServiceImpl | getAllUsers | Unexpected error retrieving paginated users: {}", e.getMessage(),
                    e);
            throw e;
        }
    }

    @Override
    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {
        log.info(
                "UserServiceImpl | searchUsers | Retrieving users with pagination - Keyword: {}, Page: {}, Size: {}, Sort: {}",
                keyword, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        try {
            Page<User> userPage = userRepository.searchUsers(keyword, pageable);
            Page<UserResponse> userResponsePage = userPage.map(userMapper::toResponse);

            log.info("UserServiceImpl | searchUsers | Found {} users on page {} of {}",
                    userResponsePage.getNumberOfElements(),
                    userResponsePage.getNumber() + 1,
                    userResponsePage.getTotalPages());

            return userResponsePage;
        } catch (DataAccessException e) {
            log.error("UserServiceImpl | searchUsers | Database error retrieving paginated users: {}", e.getMessage(),
                    e);
            return Page.empty(pageable);
        } catch (Exception e) {
            log.error("UserServiceImpl | searchUsers | Unexpected error retrieving paginated users: {}", e.getMessage(),
                    e);
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
            checkUserPhoneNumberExists(request.getPhoneNumber());

            User user = userMapper.toEntity(request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            User savedUser = userRepository.save(user);
            log.info("UserServiceImpl | createUser | Created user with id: {}", savedUser.getId());

            // Send email confirmation
            VerificationToken token = VerificationToken.builder()
                    .user(savedUser)
                    .build();

            verificationTokenRepository.save(token);
            emailService.sendEmailConfirmation(savedUser.getEmail(),
                    savedUser.getFirstName() + ' ' + savedUser.getLastName(), token.getToken());
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
    public UserResponse getCurrentUser() {
        log.info("UserServiceImpl | getCurrentUser | Getting current user details from token");
        try {
            Authentication authentication = SecurityContextHolder.getContext()
                    .getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                String userId = jwt.getClaimAsString("userId");
                log.info("UserServiceImpl | getCurrentUser | Extracted user id: {}", userId);

                User user = findUserById(userId);
                return userMapper.toResponse(user);
            } else {
                log.error("UserServiceImpl | getCurrentUser | No valid authentication found in context");
                throw new UserNotAuthorizedException("No valid authentication found");
            }
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("UserServiceImpl | getCurrentUser | Unexpected error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public UserResponse updateUser(String id, UpdateUserRequest request) {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        boolean isAdmin = false;
        String authenticatedUserId = null;

        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            authenticatedUserId = jwt.getClaimAsString("userId");
            isAdmin = authentication.getAuthorities()
                    .stream()
                    .anyMatch(a -> a.getAuthority()
                            .equals("ADMIN"));

            log.info("UserServiceImpl | updateUser | Authenticated user id: {}, isAdmin: {}", authenticatedUserId, isAdmin);
        }

        // If not admin, check if user is updating their own profile
        if (!isAdmin && (authenticatedUserId == null || !authenticatedUserId.equals(id))) {
            log.error("UserServiceImpl | updateUser | Unauthorized access: user {} attempting to update user {}",
                    authenticatedUserId, id);
            throw new UserNotAuthorizedException(id);
        }

        log.info("UserServiceImpl | updateUser | Updating user with id: {}", id);
        try {
            User user = findUserById(id);

            // Check phone number uniqueness if it changed
            if (!user.getPhoneNumber()
                    .equals(request.getPhoneNumber())) {
                checkUserPhoneNumberExists(request.getPhoneNumber());
            }

            // Update fields based on the request
            userMapper.updateEntity(request, user);

            // Only admin can modify the 'enabled' field
            if (isAdmin && request.getEnabled() != null) {
                user.setEnabled(request.getEnabled());
            }

            User updatedUser = userRepository.save(user);
            log.info("UserServiceImpl | updateUser | Updated user with id: {}", updatedUser.getId());
            return userMapper.toResponse(updatedUser);
        } catch (UserNotFoundException | UserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("UserServiceImpl | updateUser | Error updating user with id '{}': {}", id, e.getMessage(), e);
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
            if (!request.getNewPassword()
                    .equals(request.getConfirmPassword())) {
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

        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new VerificationTokenNotFoundException());

        if (LocalDateTime.now()
                .isAfter(verificationToken.getExpiryDate())) {
            throw new VerificationTokenAlreadyExpired();
        }
        User user = verificationToken.getUser();
        user.setEnabled(true);
        User savedUser = userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
        log.info("Email confirmed successfully for user: {}", savedUser.getId());
        return true;
    }

    @Override
    @Transactional
    public void resendConfirmationEmail(String email) {
        log.info("UserServiceImpl | resendConfirmationEmail | Resending confirmation email request received");

        try {
            // Find the user but don't reveal if they exist or not in error messages
            Optional<User> userOptional = userRepository.findByEmail(email);

            // Only proceed if user exists and is not enabled yet
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // If already enabled, silently return without error
                if (user.isEnabled()) {
                    log.info("UserServiceImpl | resendConfirmationEmail | User already confirmed");
                    return;
                }

                // Generate new confirmation token and update expiry
                String newToken = java.util.UUID.randomUUID()
                        .toString();
                VerificationToken verificationToken = verificationTokenRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new RuntimeException("Verification token not found"));
                verificationToken.setToken(newToken);
                verificationToken.setExpiryDate(LocalDateTime.now()
                        .plusHours(24));
                verificationTokenRepository.save(verificationToken);
                // Send email confirmation
                emailService.sendEmailConfirmation(
                        user.getEmail(),
                        user.getFirstName() + ' ' + user.getLastName(),
                        newToken);
                log.info("UserServiceImpl | resendConfirmationEmail | Confirmation email sent");
            } else {
                // User doesn't exist, but log it privately and don't expose this info
                log.info("UserServiceImpl | resendConfirmationEmail | No user found with email: {}", email);
                // Still return a success to avoid revealing user existence
            }

            // Always return success regardless of whether the email exists
            // This prevents user enumeration attacks
        } catch (DataAccessException e) {
            // Still log the actual error for debugging, but don't expose details
            log.error("UserServiceImpl | resendConfirmationEmail | Database error: {}", e.getMessage(), e);
            // Re-throw without email in message
            throw new RuntimeException("Error processing request");
        } catch (Exception e) {
            log.error("UserServiceImpl | resendConfirmationEmail | Unexpected error: {}", e.getMessage(), e);
            // Re-throw without email in message
            throw new RuntimeException("Error processing request");
        }
    }

    @Override
    @Transactional
    public void sendResetPasswordEmail(String email) {
        log.info("UserServiceImpl | sendResetPasswordEmail | Sending reset password email request received");

        try {
            // Find the user but don't reveal if they exist or not in error messages
            Optional<User> userOptional = userRepository.findByEmail(email);

            // Only proceed if user exists and is not enabled yet
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Check if there's an existing token for this user and delete it
                Optional<VerificationToken> existingToken = verificationTokenRepository.findByUserId(user.getId());
                existingToken.ifPresent(verificationTokenRepository::delete);
                // Generate new confirmation token and update expiry
                VerificationToken verificationToken = VerificationToken.builder()
                        .user(user)
                        .expiryDate(LocalDateTime.now()
                                .plusMinutes(30))
                        .build();
                verificationTokenRepository.save(verificationToken);
                // Send email confirmation
                emailService.sendResetPasswordEmail(
                        user.getEmail(),
                        user.getFirstName() + ' ' + user.getLastName(),
                        verificationToken.getToken());
                log.info("Reset password email sent to {}", user.getEmail());
            } else {
                // User doesn't exist, but log it privately and don't expose this info
                log.info("Reset password requested for non-existent email: {}", email);
                // Still return a success to avoid revealing user existence
            }

            // Always return success regardless of whether the email exists
            // This prevents user enumeration attacks
        } catch (DataAccessException e) {
            // Still log the actual error for debugging, but don't expose details
            log.error("UserServiceImpl | sendResetPasswordEmail | Database error: {}", e.getMessage(), e);
            // Re-throw without email in message
            throw new RuntimeException("Error processing request");
        } catch (Exception e) {
            log.error("UserServiceImpl | sendResetPasswordEmail | Unexpected error: {}", e.getMessage(), e);
            // Re-throw without email in message
            throw new RuntimeException("Error processing request");
        }
    }

    @Override
    public void validateResetToken(String token) {
        log.info("UserServiceImpl | validateResetToken | Validating reset token");

        try {
            VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                    .orElseThrow(() -> new VerificationTokenNotFoundException());

            if (LocalDateTime.now()
                    .isAfter(verificationToken.getExpiryDate())) {
                throw new VerificationTokenAlreadyExpired();
            }

            // If we reach here, the token is valid
            log.info("Reset token is valid for user: {}", verificationToken.getUser()
                    .getId());
        } catch (Exception e) {
            log.error("Error validating reset token: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("UserServiceImpl | resetPassword | Resetting password with token");

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }
        try {
            VerificationToken verificationToken = verificationTokenRepository.findByToken(request.getToken())
                    .orElseThrow(() -> new VerificationTokenNotFoundException());

            if (LocalDateTime.now()
                    .isAfter(verificationToken.getExpiryDate())) {
                verificationTokenRepository.delete(verificationToken);
                throw new VerificationTokenAlreadyExpired();
            }

            User user = verificationToken.getUser();

            // Update password and increment token version to invalidate other tokens
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setTokenVersion(user.getTokenVersion() + 1);

            userRepository.save(user);

            // Delete the used token
            verificationTokenRepository.delete(verificationToken);

            log.info("Password reset successfully for user: {}", user.getId());
        } catch (Exception e) {
            log.error("Error resetting password: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    protected User findUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("UserServiceImpl | findUserById | User not found with id: {}", id);
                    return new UserNotFoundException("User not found with id: " + id);
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    protected boolean doesUserExistById(String id) {
        return userRepository.existsById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    protected void checkUserEmailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            log.error("UserServiceImpl | checkUserEmailExists | User already exists with email: {}", email);
            throw new UserAlreadyExistsException(email, true);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    protected void checkUserPhoneNumberExists(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            log.error("UserServiceImpl | checkUserPhoneNumberExists | User already exists with phone number: {}",
                    phoneNumber);
            throw new UserAlreadyExistsException(phoneNumber, false);
        }
    }
}