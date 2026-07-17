package com.hospital.controller;

import com.hospital.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @GetMapping("/my")
    public Result<Map<String, Object>> myAppointments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", Collections.emptyList());
        data.put("total", 0);
        data.put("size", pageSize);
        data.put("current", page);
        data.put("pages", 0);
        return Result.ok(data);
    }

    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo) {
        return Result.ok();
    }
}
