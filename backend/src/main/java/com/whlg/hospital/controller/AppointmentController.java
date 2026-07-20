package com.whlg.hospital.controller;

import com.whlg.hospital.dto.CreateAppointmentRequest;
import com.whlg.hospital.dto.PayRequest;
import com.whlg.hospital.service.OrderService;
import com.whlg.hospital.util.R;
import com.whlg.hospital.vo.PageResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final OrderService orderService;

    public AppointmentController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public R<Map<String, Object>> create(@RequestBody CreateAppointmentRequest request) {
        return R.ok(orderService.createAppointment(request));
    }

    @GetMapping("/{orderNo}")
    public R<Map<String, Object>> detail(@PathVariable("orderNo") String orderNo) {
        return R.ok(orderService.getAppointment(orderNo));
    }

    @GetMapping("/{orderNo}/success")
    public R<Map<String, Object>> success(@PathVariable("orderNo") String orderNo) {
        return R.ok(orderService.getAppointment(orderNo));
    }

    @PostMapping("/{orderNo}/cancel")
    public R<Object> cancel(@PathVariable("orderNo") String orderNo) {
        orderService.cancelAppointment(orderNo);
        return R.ok();
    }

    @PostMapping("/{orderNo}/complete")
    public R<Object> complete(@PathVariable("orderNo") String orderNo) {
        orderService.completeAppointment(orderNo);
        return R.ok();
    }

    @PostMapping("/{orderNo}/pay")
    public R<Map<String, Object>> pay(@PathVariable("orderNo") String orderNo, @RequestBody PayRequest request) {
        return R.ok(orderService.payAppointment(orderNo, request));
    }

    @GetMapping("/my")
    public R<PageResult<Map<String, Object>>> mine(@RequestParam(required = false) Integer page,
                                                   @RequestParam(required = false) Integer pageSize,
                                                   @RequestParam(required = false) Integer status) {
        return R.ok(orderService.listMyAppointments(page, pageSize, status));
    }
}
