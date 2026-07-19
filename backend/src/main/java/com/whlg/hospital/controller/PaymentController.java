package com.whlg.hospital.controller;

import com.whlg.hospital.dto.CreatePaymentRequest;
import com.whlg.hospital.dto.PaymentCallbackRequest;
import com.whlg.hospital.service.OrderService;
import com.whlg.hospital.util.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final OrderService orderService;
    private final String callbackSecret;

    public PaymentController(OrderService orderService,
                             @Value("${payment.callback-secret:}") String callbackSecret) {
        this.orderService = orderService;
        this.callbackSecret = callbackSecret;
    }

    @PostMapping
    public R<Map<String, Object>> create(@RequestBody CreatePaymentRequest request) {
        return R.ok(orderService.createPayment(request));
    }

    @PostMapping("/callback")
    public R<Object> callback(@RequestBody PaymentCallbackRequest request,
                              @RequestHeader(value = "X-Payment-Signature", required = false) String signature) {
        if (callbackSecret.isEmpty() || signature == null
                || !MessageDigest.isEqual(callbackSecret.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))) {
            throw new com.whlg.hospital.support.ApiException(com.whlg.hospital.util.StatusCode.UNAUTHORIZED, "支付回调签名无效");
        }
        orderService.paymentCallback(request);
        return R.ok();
    }

    @GetMapping("/{businessOrderNo}")
    public R<Map<String, Object>> detail(@PathVariable("businessOrderNo") String businessOrderNo) {
        return R.ok(orderService.getPayment(businessOrderNo));
    }
}
