package com.whlg.hospital.controller;

import com.whlg.hospital.dto.CreateFeedbackRequest;
import com.whlg.hospital.service.UserCenterService;
import com.whlg.hospital.util.R;
import com.whlg.hospital.vo.PageResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    private final UserCenterService userCenterService;

    public FeedbackController(UserCenterService userCenterService) {
        this.userCenterService = userCenterService;
    }

    @GetMapping
    public R<PageResult<Map<String, Object>>> list(@RequestParam(required = false) Integer page,
                                                   @RequestParam(required = false) Integer pageSize) {
        return R.ok(userCenterService.listFeedbacks(page, pageSize));
    }

    @PostMapping
    public R<Map<String, Object>> create(@RequestBody CreateFeedbackRequest request) {
        return R.ok(userCenterService.createFeedback(request));
    }
}
