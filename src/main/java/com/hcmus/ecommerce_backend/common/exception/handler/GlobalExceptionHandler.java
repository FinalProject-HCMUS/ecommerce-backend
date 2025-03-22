package com.hcmus.ecommerce_backend.common.exception.handler;

import com.hcmus.ecommerce_backend.category.exception.CategoryAlreadyExistsException;
import com.hcmus.ecommerce_backend.category.exception.CategoryNotFoundException;
import com.hcmus.ecommerce_backend.common.model.dto.CustomError;
import com.hcmus.ecommerce_backend.product.exception.ProductAlreadyExistsException;
import com.hcmus.ecommerce_backend.product.exception.ProductNotFoundException;

import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

        /**
         * Handles MethodArgumentNotValidException thrown when validation on an argument
         * annotated with @Valid fails.
         *
         * @param ex The MethodArgumentNotValidException instance.
         * @return ResponseEntity with CustomError containing details of validation
         *         errors.
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex) {
                List<CustomError.CustomSubError> subErrors = new ArrayList<>();

                ex.getBindingResult().getAllErrors().forEach(error -> {
                        String fieldName = ((FieldError) error).getField();
                        String message = error.getDefaultMessage();
                        subErrors.add(CustomError.CustomSubError.builder()
                                        .field(fieldName)
                                        .message(message)
                                        .build());
                });

                CustomError customError = CustomError.builder()
                                .httpStatus(HttpStatus.BAD_REQUEST)
                                .header(CustomError.Header.VALIDATION_ERROR.getName())
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
         *         violations.
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
                                                                        ? invalidValue.getClass().getSimpleName()
                                                                        : null)
                                                        .build());
                                });

                CustomError customError = CustomError.builder()
                                .httpStatus(HttpStatus.BAD_REQUEST)
                                .header(CustomError.Header.VALIDATION_ERROR.getName())
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
                                .message("Database integrity constraint violated")
                                .build();

                return new ResponseEntity<>(customError, HttpStatus.CONFLICT);
        }

        /**
         * Handles RuntimeException thrown for general runtime exceptions.
         *
         * @param runtimeException The RuntimeException instance.
         * @return ResponseEntity with CustomError containing details of the runtime
         *         exception.
         */
        @ExceptionHandler(RuntimeException.class)
        protected ResponseEntity<?> handleRuntimeException(final RuntimeException runtimeException) {
                CustomError customError = CustomError.builder()
                                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                .header(CustomError.Header.API_ERROR.getName())
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
                                .message(ex.getMessage())
                                .build();

                return new ResponseEntity<>(customError, HttpStatus.CONFLICT);
        }
}
