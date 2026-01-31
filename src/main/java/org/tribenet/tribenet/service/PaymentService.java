package org.tribenet.tribenet.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tribenet.tribenet.dto.*;
import org.tribenet.tribenet.exception.PaymentException;
import org.tribenet.tribenet.exception.ResourceNotFoundException;
import org.tribenet.tribenet.model.Payment;
import org.tribenet.tribenet.model.PaymentStatus;
import org.tribenet.tribenet.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;

    public PaymentService(RazorpayClient razorpayClient, PaymentRepository paymentRepository) {
        this.razorpayClient = razorpayClient;
        this.paymentRepository = paymentRepository;
    }

    public OrderResponse createOrder(CreateOrderRequest request, Long userId) {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
            orderRequest.put("currency", request.getCurrency());
            orderRequest.put("receipt", "receipt_" + System.currentTimeMillis());

            Order order = razorpayClient.orders.create(orderRequest);

            Payment payment = new Payment();
            payment.setOrderId(order.get("id"));
            payment.setAmount(request.getAmount());
            payment.setCurrency(request.getCurrency());
            payment.setUserId(userId);
            payment.setStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);

            return new OrderResponse(
                    order.get("id"),
                    request.getAmount(),
                    request.getCurrency(),
                    order.get("status"),
                    keyId);
        } catch (RazorpayException e) {
            throw new PaymentException("Failed to create order: " + e.getMessage(), e);
        }
    }

    public PaymentResponse verifyPayment(PaymentVerificationRequest request) {
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", request.getRazorpayOrderId());
            attributes.put("razorpay_payment_id", request.getRazorpayPaymentId());
            attributes.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(attributes, keySecret);

            Payment payment = paymentRepository.findByOrderId(request.getRazorpayOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Payment not found for order: " + request.getRazorpayOrderId()));

            if (isValid) {
                payment.setPaymentId(request.getRazorpayPaymentId());
                payment.setStatus(PaymentStatus.SUCCESS);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
            }
            paymentRepository.save(payment);

            return mapToResponse(payment);
        } catch (RazorpayException e) {
            throw new PaymentException("Payment verification failed: " + e.getMessage(), e);
        }
    }

    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return mapToResponse(payment);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getPaymentId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),
                payment.getCreatedAt());
    }
}
