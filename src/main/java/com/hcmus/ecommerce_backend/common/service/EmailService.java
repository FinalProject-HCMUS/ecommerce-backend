package com.hcmus.ecommerce_backend.common.service;


import com.hcmus.ecommerce_backend.order.model.dto.response.OrderDetailWithProductResponse;
import com.hcmus.ecommerce_backend.order.model.enums.PaymentMethod;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailService {
    void sendEmailConfirmation(String email, String name, String token);

    void sendResetPasswordEmail(String email, String name, String token);

    void sendOrderConfirmationEmail(String email, String name, String orderId, Double total,
                                    List<OrderDetailWithProductResponse> orderItems, String address, Double subTotal, Double shippingCost,
                                    PaymentMethod paymentMethod, LocalDateTime orderDate);
}