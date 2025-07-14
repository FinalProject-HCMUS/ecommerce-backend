package com.hcmus.ecommerce_backend.integration.common.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmus.ecommerce_backend.common.controller.PaymentController;
import com.hcmus.ecommerce_backend.common.exception.PaymentException;
import com.hcmus.ecommerce_backend.common.model.entity.SystemSetting;
import com.hcmus.ecommerce_backend.common.model.enums.SettingDataType;
import com.hcmus.ecommerce_backend.common.repository.SystemSettingRepository;
import com.hcmus.ecommerce_backend.common.service.PaymentService;
import com.hcmus.ecommerce_backend.order.model.dto.request.CheckoutOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.CheckoutRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderResponse;
import com.hcmus.ecommerce_backend.order.model.enums.PaymentMethod;
import com.hcmus.ecommerce_backend.order.model.enums.Status;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerIntegrationTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private SystemSettingRepository systemSettingRepository;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private CheckoutRequest checkoutRequest;
    private OrderResponse orderResponse;
    private SystemSetting frontendUrlSetting;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Register JavaTimeModule for LocalDateTime

        // Setup test data
        checkoutRequest = CheckoutRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .customerId("customer-1")
                .address("123 Test Street")
                .orderDetails(Arrays.asList(
                        CheckoutOrderDetailRequest.builder()
                                .itemId("item-1")
                                .quantity(2)
                                .build()
                ))
                .build();

        orderResponse = OrderResponse.builder()
                .id("order-1")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .customerId("customer-1")
                .address("123 Test Street")
                .paymentMethod(PaymentMethod.VN_PAY)
                .status(Status.NEW)
                .isPaid(false)
                .total(110.0)
                .createdAt(LocalDateTime.now())
                .build();

        frontendUrlSetting = SystemSetting.builder()
                .key("frontend-url")
                .value("http://localhost:3000")
                .serviceName("MySetting")
                .type(SettingDataType.String)
                .build();
    }

    @Test
    void createVNPayPayment_WithNullRequest_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/vn-payment/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).checkoutVNPay(any());
        verify(paymentService, never()).createPaymentUrl(anyString(), anyDouble());
    }

    @Test
    void createVNPayPayment_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/vn-payment/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).checkoutVNPay(any());
        verify(paymentService, never()).createPaymentUrl(anyString(), anyDouble());
    }

    @Test
    void createVNPayPayment_WithEmptyRequestBody_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/vn-payment/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).checkoutVNPay(any());
        verify(paymentService, never()).createPaymentUrl(anyString(), anyDouble());
    }

    // Test GET /vn-payment/return
    @Test
    void handleReturn_WithValidPaymentResponse_ShouldRedirectToSuccessPage() throws Exception {
        // Given
        when(paymentService.validatePaymentResponse(any(Map.class))).thenReturn(true);
        when(systemSettingRepository.findByKey("frontend-url")).thenReturn(Optional.of(frontendUrlSetting));

        // When & Then
        mockMvc.perform(get("/vn-payment/return")
                .param("vnp_ResponseCode", "00")
                .param("vnp_TxnRef", "order-1")
                .param("vnp_Amount", "11000")
                .param("vnp_SecureHash", "ABC123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:3000/confirm-vnpay/success"));

        verify(paymentService).validatePaymentResponse(any(Map.class));
        verify(systemSettingRepository).findByKey("frontend-url");
    }

    @Test
    void handleReturn_WithInvalidPaymentResponse_ShouldRedirectToFailurePage() throws Exception {
        // Given
        when(paymentService.validatePaymentResponse(any(Map.class))).thenReturn(false);
        when(systemSettingRepository.findByKey("frontend-url")).thenReturn(Optional.of(frontendUrlSetting));

        // When & Then
        mockMvc.perform(get("/vn-payment/return")
                .param("vnp_ResponseCode", "01")
                .param("vnp_TxnRef", "order-1")
                .param("vnp_Amount", "11000")
                .param("vnp_SecureHash", "ABC123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:3000/confirm-vnpay/failure"));

        verify(paymentService).validatePaymentResponse(any(Map.class));
        verify(systemSettingRepository).findByKey("frontend-url");
    }

    @Test
    void handleReturn_WithPaymentServiceException_ShouldRedirectToErrorPage() throws Exception {
        // Given
        when(paymentService.validatePaymentResponse(any(Map.class)))
                .thenThrow(new PaymentException("Payment validation failed"));
        when(systemSettingRepository.findByKey("frontend-url")).thenReturn(Optional.of(frontendUrlSetting));

        // When & Then
        mockMvc.perform(get("/vn-payment/return")
                .param("vnp_ResponseCode", "00")
                .param("vnp_TxnRef", "order-1")
                .param("vnp_Amount", "11000")
                .param("vnp_SecureHash", "ABC123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:3000/confirm-vnpay/error"));

        verify(paymentService).validatePaymentResponse(any(Map.class));
        verify(systemSettingRepository).findByKey("frontend-url");
    }

    @Test
    void handleReturn_WithNoFrontendUrlSetting_ShouldUseDefaultUrl() throws Exception {
        // Given
        when(paymentService.validatePaymentResponse(any(Map.class))).thenReturn(true);
        when(systemSettingRepository.findByKey("frontend-url")).thenReturn(Optional.empty());
        when(systemSettingRepository.findByServiceName("MySetting")).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/vn-payment/return")
                .param("vnp_ResponseCode", "00")
                .param("vnp_TxnRef", "order-1")
                .param("vnp_Amount", "11000")
                .param("vnp_SecureHash", "ABC123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:3000/confirm-vnpay/success"));

        verify(paymentService).validatePaymentResponse(any(Map.class));
        verify(systemSettingRepository).findByKey("frontend-url");
        verify(systemSettingRepository).findByServiceName("MySetting");
    }

    @Test
    void handleReturn_WithFrontendUrlFromMySettingService_ShouldUseServiceUrl() throws Exception {
        // Given
        SystemSetting mySettingUrl = SystemSetting.builder()
                .key("FrontendUrl")
                .value("http://custom-frontend.com")
                .serviceName("MySetting")
                .type(SettingDataType.String)
                .build();
        
        when(paymentService.validatePaymentResponse(any(Map.class))).thenReturn(true);
        when(systemSettingRepository.findByKey("frontend-url")).thenReturn(Optional.empty());
        when(systemSettingRepository.findByServiceName("MySetting")).thenReturn(Arrays.asList(mySettingUrl));

        // When & Then
        mockMvc.perform(get("/vn-payment/return")
                .param("vnp_ResponseCode", "00")
                .param("vnp_TxnRef", "order-1")
                .param("vnp_Amount", "11000")
                .param("vnp_SecureHash", "ABC123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://custom-frontend.com/confirm-vnpay/success"));

        verify(paymentService).validatePaymentResponse(any(Map.class));
        verify(systemSettingRepository).findByKey("frontend-url");
        verify(systemSettingRepository).findByServiceName("MySetting");
    }

    @Test
    void handleReturn_WithSystemSettingException_ShouldUseDefaultUrl() throws Exception {
        // Given
        when(paymentService.validatePaymentResponse(any(Map.class))).thenReturn(true);
        when(systemSettingRepository.findByKey("frontend-url"))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/vn-payment/return")
                .param("vnp_ResponseCode", "00")
                .param("vnp_TxnRef", "order-1")
                .param("vnp_Amount", "11000")
                .param("vnp_SecureHash", "ABC123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:3000/confirm-vnpay/success"));

        verify(paymentService).validatePaymentResponse(any(Map.class));
        verify(systemSettingRepository).findByKey("frontend-url");
    }

    @Test
    void handleReturn_WithNoParams_ShouldStillHandleGracefully() throws Exception {
        // Given
        when(paymentService.validatePaymentResponse(any(Map.class))).thenReturn(false);
        when(systemSettingRepository.findByKey("frontend-url")).thenReturn(Optional.of(frontendUrlSetting));

        // When & Then
        mockMvc.perform(get("/vn-payment/return"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:3000/confirm-vnpay/failure"));

        verify(paymentService).validatePaymentResponse(any(Map.class));
        verify(systemSettingRepository).findByKey("frontend-url");
    }

    @Test
    void handleReturn_WithMultipleParams_ShouldPassAllToService() throws Exception {
        // Given
        when(paymentService.validatePaymentResponse(any(Map.class))).thenReturn(true);
        when(systemSettingRepository.findByKey("frontend-url")).thenReturn(Optional.of(frontendUrlSetting));

        // When & Then
        mockMvc.perform(get("/vn-payment/return")
                .param("vnp_ResponseCode", "00")
                .param("vnp_TxnRef", "order-1")
                .param("vnp_Amount", "11000")
                .param("vnp_SecureHash", "ABC123")
                .param("vnp_PayDate", "20240714100000")
                .param("vnp_TransactionNo", "123456789")
                .param("vnp_BankCode", "NCB"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:3000/confirm-vnpay/success"));

        // Verify all parameters were passed to service
        verify(paymentService).validatePaymentResponse(argThat(params -> 
            params.containsKey("vnp_ResponseCode") &&
            params.containsKey("vnp_TxnRef") &&
            params.containsKey("vnp_Amount") &&
            params.containsKey("vnp_SecureHash") &&
            params.containsKey("vnp_PayDate") &&
            params.containsKey("vnp_TransactionNo") &&
            params.containsKey("vnp_BankCode")
        ));
    }

    @Test
    void handleReturn_WithCustomFrontendUrl_ShouldUseCustomUrl() throws Exception {
        // Given
        SystemSetting customFrontendUrl = SystemSetting.builder()
                .key("frontend-url")
                .value("https://my-custom-frontend.com")
                .serviceName("MySetting")
                .type(SettingDataType.String)
                .build();

        when(paymentService.validatePaymentResponse(any(Map.class))).thenReturn(true);
        when(systemSettingRepository.findByKey("frontend-url")).thenReturn(Optional.of(customFrontendUrl));

        // When & Then
        mockMvc.perform(get("/vn-payment/return")
                .param("vnp_ResponseCode", "00")
                .param("vnp_TxnRef", "order-1")
                .param("vnp_Amount", "11000")
                .param("vnp_SecureHash", "ABC123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://my-custom-frontend.com/confirm-vnpay/success"));

        verify(paymentService).validatePaymentResponse(any(Map.class));
        verify(systemSettingRepository).findByKey("frontend-url");
    }

    // Test POST /vn-payment/retry/{orderId}
    @Test
    void retryVNPayPayment_WithValidOrderId_ShouldReturnPaymentUrl() throws Exception {
        // Given
        String orderId = "order-1";
        String expectedPaymentUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=11000&vnp_Command=pay&vnp_CreateDate=20240714100000&vnp_CurrCode=VND&vnp_IpAddr=127.0.0.1&vnp_Locale=vn&vnp_OrderInfo=Thanh+toan+don+hang+order-1&vnp_OrderType=other&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A8080%2Freturn&vnp_TmnCode=DEMO&vnp_TxnRef=order-1&vnp_Version=2.1.0&vnp_SecureHash=ABC123";
        
        when(paymentService.createRetryPaymentUrl(orderId)).thenReturn(expectedPaymentUrl);

        // When & Then
        mockMvc.perform(post("/vn-payment/retry/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedPaymentUrl));

        verify(paymentService).createRetryPaymentUrl(orderId);
    }

    @Test
    void retryVNPayPayment_WithAlreadyPaidOrder_ShouldReturnInternalServerError() throws Exception {
        // Given
        String orderId = "paid-order";
        when(paymentService.createRetryPaymentUrl(orderId))
                .thenThrow(new PaymentException("Order already paid"));

        // When & Then
        mockMvc.perform(post("/vn-payment/retry/{orderId}", orderId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error creating retry VNPay payment URL: Order already paid"));

        verify(paymentService).createRetryPaymentUrl(orderId);
    }

    @Test
    void retryVNPayPayment_WithPaymentServiceException_ShouldReturnInternalServerError() throws Exception {
        // Given
        String orderId = "order-1";
        when(paymentService.createRetryPaymentUrl(orderId))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        mockMvc.perform(post("/vn-payment/retry/{orderId}", orderId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error creating retry VNPay payment URL: Unexpected error"));

        verify(paymentService).createRetryPaymentUrl(orderId);
    }

    @Test
    void retryVNPayPayment_WithSpecialCharactersInOrderId_ShouldWork() throws Exception {
        // Given
        String orderId = "order-123_special";
        String expectedPaymentUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_TxnRef=" + orderId;
        
        when(paymentService.createRetryPaymentUrl(orderId)).thenReturn(expectedPaymentUrl);

        // When & Then
        mockMvc.perform(post("/vn-payment/retry/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedPaymentUrl));

        verify(paymentService).createRetryPaymentUrl(orderId);
    }

    @Test
    void retryVNPayPayment_WithLongOrderId_ShouldWork() throws Exception {
        // Given
        String longOrderId = "order-1234567890123456789012345678901234567890";
        String expectedPaymentUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_TxnRef=" + longOrderId;
        
        when(paymentService.createRetryPaymentUrl(longOrderId)).thenReturn(expectedPaymentUrl);

        // When & Then
        mockMvc.perform(post("/vn-payment/retry/{orderId}", longOrderId))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedPaymentUrl));

        verify(paymentService).createRetryPaymentUrl(longOrderId);
    }

    @Test
    void retryVNPayPayment_WithEmptyOrderId_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(post("/vn-payment/retry/"))
                .andExpect(status().isNotFound());

        verify(paymentService, never()).createRetryPaymentUrl(anyString());
    }

    @Test
    void handleReturn_WithWrongServiceName_ShouldFallbackToDefault() throws Exception {
        // Given
        SystemSetting wrongServiceSetting = SystemSetting.builder()
                .key("frontend-url")
                .value("http://wrong-service.com")
                .serviceName("WrongService")
                .type(SettingDataType.String)
                .build();
        
        when(paymentService.validatePaymentResponse(any(Map.class))).thenReturn(true);
        when(systemSettingRepository.findByKey("frontend-url")).thenReturn(Optional.of(wrongServiceSetting));
        when(systemSettingRepository.findByServiceName("MySetting")).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/vn-payment/return")
                .param("vnp_ResponseCode", "00")
                .param("vnp_TxnRef", "order-1")
                .param("vnp_Amount", "11000")
                .param("vnp_SecureHash", "ABC123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:3000/confirm-vnpay/success"));

        verify(paymentService).validatePaymentResponse(any(Map.class));
        verify(systemSettingRepository).findByKey("frontend-url");
        verify(systemSettingRepository).findByServiceName("MySetting");
    }

    @Test
    void handleReturn_WithRuntimeException_ShouldRedirectToErrorPage() throws Exception {
        // Given
        when(paymentService.validatePaymentResponse(any(Map.class)))
                .thenThrow(new RuntimeException("Unexpected error"));
        when(systemSettingRepository.findByKey("frontend-url")).thenReturn(Optional.of(frontendUrlSetting));

        // When & Then
        mockMvc.perform(get("/vn-payment/return")
                .param("vnp_ResponseCode", "00")
                .param("vnp_TxnRef", "order-1")
                .param("vnp_Amount", "11000")
                .param("vnp_SecureHash", "ABC123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:3000/confirm-vnpay/error"));

        verify(paymentService).validatePaymentResponse(any(Map.class));
        verify(systemSettingRepository).findByKey("frontend-url");
    }
}