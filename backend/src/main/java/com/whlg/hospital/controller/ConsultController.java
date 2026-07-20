package com.whlg.hospital.controller;

import com.whlg.hospital.dto.CreateConsultRequest;
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
@RequestMapping("/api/consults")
public class ConsultController {

    private final OrderService orderService;

    public ConsultController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public R<Map<String, Object>> create(@RequestBody CreateConsultRequest request) {
        return R.ok(orderService.createConsult(request));
    }

    @GetMapping("/{orderNo}")
    public R<Map<String, Object>> detail(@PathVariable("orderNo") String orderNo) {
        return R.ok(orderService.getConsult(orderNo));
    }

    @GetMapping("/{orderNo}/success")
    public R<Map<String, Object>> success(@PathVariable("orderNo") String orderNo) {
        return R.ok(orderService.getConsult(orderNo));
    }

    @PostMapping("/{orderNo}/cancel")
    public R<Object> cancel(@PathVariable("orderNo") String orderNo) {
        orderService.cancelConsult(orderNo);
        return R.ok();
    }

    @PostMapping("/{orderNo}/pay")
    public R<Map<String, Object>> pay(@PathVariable("orderNo") String orderNo, @RequestBody PayRequest request) {
        return R.ok(orderService.payConsult(orderNo, request));
    }

    @GetMapping("/my")
    public R<PageResult<Map<String, Object>>> mine(@RequestParam(required = false) Integer page,
                                                   @RequestParam(required = false) Integer pageSize,
                                                   @RequestParam(required = false) Integer status) {
        return R.ok(orderService.listMyConsults(page, pageSize, status));
    }
}
