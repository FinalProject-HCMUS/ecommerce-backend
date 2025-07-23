package com.hcmus.ecommerce_backend.common.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

import com.hcmus.ecommerce_backend.common.model.entity.SystemSetting;
import com.hcmus.ecommerce_backend.common.repository.SystemSettingRepository;
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
    private final SystemSettingRepository systemSettingRepository;

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
    public ResponseEntity<Void> handleReturn(@RequestParam Map<String, String> params) {
        log.info("VNPayController | handleReturn | VNPay response: {}", params);
        boolean isValid;
        String statusCode;
        
        try {
            isValid = paymentService.validatePaymentResponse(params);
            statusCode = isValid ? "success" : "failure";
        } catch (Exception e) {
            log.error("VNPayController | handleReturn | Error processing payment: {}", e.getMessage(), e);
            statusCode = "error";
            isValid = false;
        }
        
        // URL frontend cứng
        String frontendUrl = getFrontendUrlFromSystemSetting();
        
        // Tạo URL redirect đến frontend
        String redirectUrl = frontendUrl + "/confirm-vnpay/" + statusCode;
        
        // Thêm mã giao dịch vào URL nếu có
        // if (params.containsKey("vnp_TxnRef")) {
        //     String txnRef = params.get("vnp_TxnRef");
        //     redirectUrl += "?txnRef=" + txnRef;
        // }
        
        log.info("VNPayController | handleReturn | Redirecting to: {}", redirectUrl);
        
        // Redirect bằng cách đặt header Location
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", redirectUrl);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * Lấy URL frontend từ bảng SystemSetting
     * Nếu không tìm thấy, quay lại lấy từ application.yml
     * Nếu vẫn không tìm thấy, dùng giá trị mặc định
     */
    private String getFrontendUrlFromSystemSetting() {
        try {
            // Tìm setting với serviceName là MySetting và key là FrontendUrl
            Optional<SystemSetting> setting = systemSettingRepository.findByKey("FrontendUrl");
            
            if (setting.isPresent() && setting.get().getServiceName().equals("MySetting")) {
                log.info("Using frontend URL from SystemSetting: {}", setting.get().getValue());
                return setting.get().getValue();
            }
            
            // Thử lấy từ các setting của service MySetting
            List<SystemSetting> mySettings = systemSettingRepository.findByServiceName("MySetting");
            Optional<SystemSetting> frontendUrlSetting = mySettings.stream()
                    .filter(s -> s.getKey().equals("frontend-url") || s.getKey().equals("FrontendUrl"))
                    .findFirst();
            
            if (frontendUrlSetting.isPresent()) {
                log.info("Using frontend URL from MySetting service: {}", frontendUrlSetting.get().getValue());
                return frontendUrlSetting.get().getValue();
            }
        } catch (Exception e) {
            log.error("Error retrieving frontend URL from SystemSetting: {}", e.getMessage(), e);
        }
        
        // Nếu không tìm thấy ở đâu, dùng giá trị mặc định
        log.info("Using default frontend URL: https://finalproject-hcmus.github.io/ecommerce-customer/");
        return "https://finalproject-hcmus.github.io/ecommerce-customer/";
    }   

    @PostMapping("/retry/{orderId}")
    @Operation(summary = "Retry VNPay payment", description = "Creates a new VNPay payment URL for an existing failed order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created retry VNPay payment URL"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Order already paid or cannot retry"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> retryVNPayPayment(
        @Parameter(description = "Order ID to retry payment", required = true)
        @PathVariable String orderId) {
        
        log.info("VNPayController | retryVNPayPayment | Retrying VNPay payment for order: {}", orderId);
        try {
            // Tạo lại payment URL cho order đã tồn tại
            String paymentUrl = paymentService.createRetryPaymentUrl(orderId);
            
            log.info("VNPayController | retryVNPayPayment | Retry VNPay payment URL created: {}", paymentUrl);
            return ResponseEntity.ok(paymentUrl);
        } catch (Exception e) {
            log.error("VNPayController | retryVNPayPayment | Error creating retry VNPay payment URL: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating retry VNPay payment URL: " + e.getMessage());
        }
    }
}
