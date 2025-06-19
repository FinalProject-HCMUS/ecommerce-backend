package com.hcmus.ecommerce_backend.common.exception.handler;

import com.hcmus.ecommerce_backend.auth.exception.PasswordNotValidException;
import com.hcmus.ecommerce_backend.auth.exception.UserNotActivatedException;
import com.hcmus.ecommerce_backend.blog.exception.BlogAlreadyExistsException;
import com.hcmus.ecommerce_backend.blog.exception.BlogNotFoundException;
import com.hcmus.ecommerce_backend.category.exception.CategoryAlreadyExistsException;
import com.hcmus.ecommerce_backend.category.exception.CategoryNotFoundException;
import com.hcmus.ecommerce_backend.common.exception.ImageUploadException;
import com.hcmus.ecommerce_backend.common.exception.KeyNotFoundException;
import com.hcmus.ecommerce_backend.common.model.dto.CustomError;
import com.hcmus.ecommerce_backend.message.exception.ConversationAlreadyExistsException;
import com.hcmus.ecommerce_backend.message.exception.ConversationNotFoundException;
import com.hcmus.ecommerce_backend.message.exception.MessageAlreadyExistsException;
import com.hcmus.ecommerce_backend.message.exception.MessageNotFoundException;
import com.hcmus.ecommerce_backend.order.exception.InsufficientInventoryException;
import com.hcmus.ecommerce_backend.order.exception.OrderAlreadyExistsException;
import com.hcmus.ecommerce_backend.order.exception.OrderNotFoundException;
import com.hcmus.ecommerce_backend.order.exception.OrderTrackAlreadyExistsException;
import com.hcmus.ecommerce_backend.order.exception.OrderTrackNotFoundException;
import com.hcmus.ecommerce_backend.product.exception.CartItemAlreadyExistsException;
import com.hcmus.ecommerce_backend.product.exception.CartItemNotFoundException;
import com.hcmus.ecommerce_backend.product.exception.ProductAlreadyExistsException;
import com.hcmus.ecommerce_backend.product.exception.ProductNotFoundException;
import com.hcmus.ecommerce_backend.review.exception.ReviewAlreadyExistsException;
import com.hcmus.ecommerce_backend.review.exception.ReviewNotFoundException;
import com.hcmus.ecommerce_backend.user.exception.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles NoHandlerFoundException thrown when a requested endpoint is not
     * found.
     *
     * @param ex The NoHandlerFoundException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            final org.springframework.web.servlet.NoHandlerFoundException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .header(CustomError.Header.NOT_FOUND.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message("The requested resource was not found: " + ex.getRequestURL())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles MethodArgumentNotValidException thrown when validation on an argument
     * annotated with @Valid fails.
     *
     * @param ex The MethodArgumentNotValidException instance.
     * @return ResponseEntity with CustomError containing details of validation
     * errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex) {
        List<CustomError.CustomSubError> subErrors = new ArrayList<>();

        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String message = error.getDefaultMessage();
                    log.info(String.valueOf(error));
                    subErrors.add(CustomError.CustomSubError.builder()
                            .field(fieldName)
                            .message(message)
                            .build());
                });

        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.VALIDATION_ERROR.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message("Validation failed")
                .subErrors(subErrors)
                .build();

        return new ResponseEntity<>(customError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles ConstraintViolationException thrown when a bean validation constraint
     * is violated.
     *
     * @param constraintViolationException The ConstraintViolationException
     *                                     instance.
     * @return ResponseEntity with CustomError containing details of constraint
     * violations.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(
            final ConstraintViolationException constraintViolationException) {

        List<CustomError.CustomSubError> subErrors = new ArrayList<>();
        constraintViolationException.getConstraintViolations()
                .forEach(constraintViolation -> {
                    Object invalidValue = constraintViolation.getInvalidValue();
                    subErrors.add(CustomError.CustomSubError.builder()
                            .message(constraintViolation.getMessage())
                            .field(StringUtils.substringAfterLast(
                                    constraintViolation.getPropertyPath()
                                            .toString(),
                                    "."))
                            .value(invalidValue != null ? invalidValue.toString() : null)
                            .type(invalidValue != null
                                    ? invalidValue.getClass()
                                    .getSimpleName()
                                    : null)
                            .build());
                });

        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.VALIDATION_ERROR.getName())
                .exceptionName(constraintViolationException.getClass()
                        .getSimpleName())
                .message("Constraint violation")
                .subErrors(subErrors)
                .build();

        return new ResponseEntity<>(customError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles CategoryNotFoundException thrown when a category is not found.
     *
     * @param ex The CategoryNotFoundException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    protected ResponseEntity<Object> handleCategoryNotFoundException(final CategoryNotFoundException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .header(CustomError.Header.NOT_FOUND.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles DataIntegrityViolationException thrown when there is a data integrity
     * violation.
     *
     * @param ex The DataIntegrityViolationException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolation(final DataIntegrityViolationException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .header(CustomError.Header.DATABASE_ERROR.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message("Database integrity constraint violated")
                .build();

        return new ResponseEntity<>(customError, HttpStatus.CONFLICT);
    }

    /**
     * Handles RuntimeException thrown for general runtime exceptions.
     *
     * @param runtimeException The RuntimeException instance.
     * @return ResponseEntity with CustomError containing details of the runtime
     * exception.
     */
    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<?> handleRuntimeException(final RuntimeException runtimeException) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(CustomError.Header.API_ERROR.getName())
                .exceptionName(runtimeException.getClass()
                        .getSimpleName())
                .message(runtimeException.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles CategoryAlreadyExistsException thrown when a category already exists.
     *
     * @param ex The CategoryAlreadyExistsException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(CategoryAlreadyExistsException.class)
    protected ResponseEntity<Object> handleCategoryAlreadyExistsException(final CategoryAlreadyExistsException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .header(CustomError.Header.ALREADY_EXIST.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.CONFLICT);
    }

    /**
     * Handles ProductNotFoundException thrown when a product is not found.
     *
     * @param ex The ProductNotFoundException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(ProductNotFoundException.class)
    protected ResponseEntity<Object> handleProductNotFoundException(final ProductNotFoundException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .header(CustomError.Header.NOT_FOUND.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles ProductAlreadyExistsException thrown when a product already exists.
     *
     * @param ex The ProductAlreadyExistsException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(ProductAlreadyExistsException.class)
    protected ResponseEntity<Object> handleProductAlreadyExistsException(final ProductAlreadyExistsException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .header(CustomError.Header.ALREADY_EXIST.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.CONFLICT);
    }

    /**
     * Handles OrderNotFoundException thrown when an order is not found.
     *
     * @param ex The OrderNotFoundException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(OrderNotFoundException.class)
    protected ResponseEntity<Object> handleOrderNotFoundException(final OrderNotFoundException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .header(CustomError.Header.NOT_FOUND.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles OrderAlreadyExistsException thrown when an order already exists.
     *
     * @param ex The OrderAlreadyExistsException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(OrderAlreadyExistsException.class)
    protected ResponseEntity<Object> handleOrderAlreadyExistsException(final OrderAlreadyExistsException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .header(CustomError.Header.ALREADY_EXIST.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.CONFLICT);
    }

    /**
     * Handles CartItemNotFoundException thrown when a cart item is not found.
     *
     * @param ex The CartItemNotFoundException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(CartItemNotFoundException.class)
    protected ResponseEntity<Object> handleCartItemNotFoundException(final CartItemNotFoundException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .header(CustomError.Header.NOT_FOUND.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles CartItemAlreadyExistsException thrown when a cart item already
     * exists.
     *
     * @param ex The CartItemAlreadyExistsException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(CartItemAlreadyExistsException.class)
    protected ResponseEntity<Object> handleCartItemAlreadyExistsException(final CartItemAlreadyExistsException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .header(CustomError.Header.ALREADY_EXIST.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.CONFLICT);
    }

    /**
     * Handles OrderTrackNotFoundException thrown when an order track is not found.
     *
     * @param ex The OrderTrackNotFoundException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(OrderTrackNotFoundException.class)
    protected ResponseEntity<Object> handleOrderTrackNotFoundException(final OrderTrackNotFoundException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .header(CustomError.Header.NOT_FOUND.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles OrderTrackAlreadyExistsException thrown when an order track already
     * exists.
     *
     * @param ex The OrderTrackAlreadyExistsException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(OrderTrackAlreadyExistsException.class)
    protected ResponseEntity<Object> handleOrderTrackAlreadyExistsException(
            final OrderTrackAlreadyExistsException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .header(CustomError.Header.ALREADY_EXIST.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.CONFLICT);
    }

    /**
     * Handles ReviewNotFoundException thrown when a review is not found.
     *
     * @param ex The ReviewNotFoundException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(ReviewNotFoundException.class)
    protected ResponseEntity<Object> handleReviewNotFoundException(final ReviewNotFoundException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .header(CustomError.Header.NOT_FOUND.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles ReviewAlreadyExistsException thrown when a review already exists.
     *
     * @param ex The ReviewAlreadyExistsException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(ReviewAlreadyExistsException.class)
    protected ResponseEntity<Object> handleReviewAlreadyExistsException(final ReviewAlreadyExistsException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .header(CustomError.Header.ALREADY_EXIST.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.CONFLICT);
    }

    /**
     * Handles MessageNotFoundException thrown when a message is not found.
     *
     * @param ex The MessageNotFoundException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(MessageNotFoundException.class)
    protected ResponseEntity<Object> handleMessageNotFoundException(final MessageNotFoundException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .header(CustomError.Header.NOT_FOUND.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles MessageAlreadyExistsException thrown when a message already exists.
     *
     * @param ex The MessageAlreadyExistsException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(MessageAlreadyExistsException.class)
    protected ResponseEntity<Object> handleMessageAlreadyExistsException(final MessageAlreadyExistsException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .header(CustomError.Header.ALREADY_EXIST.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.CONFLICT);
    }

    /**
     * Handles UserAlreadyExistsException thrown when a user already exists.
     *
     * @param ex The UserAlreadyExistsException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    protected ResponseEntity<Object> handleUserAlreadyExistsException(final UserAlreadyExistsException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .header(CustomError.Header.ALREADY_EXIST.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.CONFLICT);
    }

    /**
     * Handles UserNotFoundException thrown when a user is not found.
     *
     * @param ex The UserNotFoundException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(UserNotFoundException.class)
    protected ResponseEntity<Object> handleUserNotFoundException(final UserNotFoundException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .header(CustomError.Header.NOT_FOUND.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles UserNotAuthorizedException thrown when a user is not authorized.
     *
     * @param ex The UserNotAuthorizedException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(UserNotAuthorizedException.class)
    protected ResponseEntity<Object> handleUserNotAuthorizedException(final UserNotAuthorizedException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.FORBIDDEN)
                .header(CustomError.Header.AUTH_ERROR.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles VerificationTokenAlreadyExpired thrown when a verification token has
     * expired.
     *
     * @param ex The VerificationTokenAlreadyExpired instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(VerificationTokenAlreadyExpired.class)
    protected ResponseEntity<Object> handleVerificationTokenAlreadyExpired(
            final VerificationTokenAlreadyExpired ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.PROCESS_ERROR.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles EmailAlreadyConfirmedException thrown when an email is already
     * confirmed.
     *
     * @param ex The EmailAlreadyConfirmedException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(EmailAlreadyConfirmedException.class)
    protected ResponseEntity<Object> handleEmailAlreadyConfirmedException(final EmailAlreadyConfirmedException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.PROCESS_ERROR.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles VerificationTokenNotFoundException thrown when a verification token
     * is not found.
     *
     * @param ex The VerificationTokenNotFoundException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(VerificationTokenNotFoundException.class)
    protected ResponseEntity<Object> handleVerificationTokenNotFoundException(
            final VerificationTokenNotFoundException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.NOT_FOUND.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConversationNotFoundException.class)
    public ResponseEntity<Object> handleConversationNotFoundException(final
                                                                      ConversationNotFoundException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .header(CustomError.Header.NOT_FOUND.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(customError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConversationAlreadyExistsException.class)
    public ResponseEntity<Object> handleConversationAlreadyExistsException(
            final ConversationAlreadyExistsException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .header(CustomError.Header.ALREADY_EXIST.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(customError, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<Object> handleImageUploadException(
            ImageUploadException exception) {
        log.error("GlobalExceptionHandler | handleImageUploadException | {}", exception.getMessage(), exception);

        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(CustomError.Header.PROCESS_ERROR.getName())
                .exceptionName(exception.getClass()
                        .getSimpleName())
                .message(exception.getMessage())
                .build();
        return new ResponseEntity<>(customError, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> handleIOException(
            IOException exception) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(CustomError.Header.PROCESS_ERROR.getName())
                .exceptionName(exception.getClass()
                        .getSimpleName())
                .message(exception.getMessage())
                .build();
        return new ResponseEntity<>(customError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles BlogNotFoundException thrown when a blog is not found.
     *
     * @param ex The BlogNotFoundException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(BlogNotFoundException.class)
    protected ResponseEntity<Object> handleBlogNotFoundException(final BlogNotFoundException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .header(CustomError.Header.NOT_FOUND.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles BlogAlreadyExistsException thrown when a blog already exists.
     *
     * @param ex The BlogAlreadyExistsException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(BlogAlreadyExistsException.class)
    protected ResponseEntity<Object> handleBlogAlreadyExistsException(final BlogAlreadyExistsException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .header(CustomError.Header.ALREADY_EXIST.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.CONFLICT);
    }

    /**
     * Handles UserNotActivatedException thrown when a user account is not activated.
     *
     * @param ex The UserNotActivatedException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(UserNotActivatedException.class)
    protected ResponseEntity<Object> handleUserNotActivatedException(final UserNotActivatedException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.FORBIDDEN)
                .header(CustomError.Header.AUTH_ERROR.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.FORBIDDEN);
    }

        /**
     * Handles KeyNotFoundException thrown when a key is not found.
     *
     * @param ex The KeyNotFoundException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(KeyNotFoundException.class)
    protected ResponseEntity<Object> handleKeyNotFoundException(final KeyNotFoundException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .header(CustomError.Header.NOT_FOUND.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles InsufficientInventoryException thrown when there is insufficient inventory.
     *
     * @param ex The InsufficientInventoryException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(InsufficientInventoryException.class)
    protected ResponseEntity<Object> handleInsufficientInventoryException(final InsufficientInventoryException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.PROCESS_ERROR.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles PasswordNotValidException thrown when a password is not valid.
     *
     * @param ex The PasswordNotValidException instance.
     * @return ResponseEntity with CustomError containing details of the exception.
     */
    @ExceptionHandler(PasswordNotValidException.class)
    protected ResponseEntity<Object> handlePasswordNotValidException(final PasswordNotValidException ex) {
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.VALIDATION_ERROR.getName())
                .exceptionName(ex.getClass()
                        .getSimpleName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(customError, HttpStatus.BAD_REQUEST);
    }
}
