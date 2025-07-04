package com.hcmus.ecommerce_backend.common.service;

import java.util.Map;

import com.hcmus.ecommerce_backend.order.model.dto.request.CheckoutRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderResponse;

public interface PaymentService {
    String createPaymentUrl(String orderId, double amount);
    boolean validatePaymentResponse(Map<String, String> params);
    OrderResponse checkoutVNPay(CheckoutRequest request);
    String createRetryPaymentUrl(String orderId);
}