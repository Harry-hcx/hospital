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
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final OrderService orderService;

    public PaymentController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public R<Map<String, Object>> create(@RequestBody CreatePaymentRequest request) {
        return R.ok(orderService.createPayment(request));
    }

    @PostMapping("/callback")
    public R<Object> callback(@RequestBody PaymentCallbackRequest request) {
        orderService.paymentCallback(request);
        return R.ok();
    }

    @GetMapping("/{businessOrderNo}")
    public R<Map<String, Object>> detail(@PathVariable("businessOrderNo") String businessOrderNo) {
        return R.ok(orderService.getPayment(businessOrderNo));
    }
}
