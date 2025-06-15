package com.hcmus.ecommerce_backend.common.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.ecommerce_backend.common.service.PaymentService;
import com.hcmus.ecommerce_backend.order.model.dto.request.CheckoutRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vn-payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/create")
    @Operation(summary = "Create VNPay payment URL", description = "Creates a VNPay payment URL for the given order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created VNPay payment URL"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> createVNPayPayment(
        @Parameter(description = "Order information and order details", required = true)
            @Valid @RequestBody CheckoutRequest request) {
        log.info("VNPayController | createVNPayPayment | Processing VNPay payment for customer: {}", request.getCustomerId());
        try {
            // 1. Validate and create order
            OrderResponse orderResponse = paymentService.checkoutVNPay(request);

            // 2. Generate VNPay payment URL
            String paymentUrl = paymentService.createPaymentUrl(orderResponse.getId(), orderResponse.getTotal());

            log.info("VNPayController | createVNPayPayment | VNPay payment URL created: {}", paymentUrl);
            return ResponseEntity.ok(paymentUrl);
        } catch (Exception e) {
            log.error("VNPayController | createVNPayPayment | Error creating VNPay payment URL: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating VNPay payment URL");
        }
    }

    @GetMapping("/return")
    public ResponseEntity<String> handleReturn(@RequestParam Map<String, String> params) {
        log.info("VNPayController | handleReturn | VNPay response: {}", params);
        boolean isValid = paymentService.validatePaymentResponse(params);
        if (isValid) {
            return ResponseEntity.ok("Payment successful");
        } else {
            return ResponseEntity.badRequest().body("Invalid payment response");
        }
    }
}
