package com.whlg.hospital.controller;

import com.whlg.hospital.service.UserCenterService;
import com.whlg.hospital.util.R;
import com.whlg.hospital.vo.PageResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    private final UserCenterService userCenterService;

    public FollowController(UserCenterService userCenterService) {
        this.userCenterService = userCenterService;
    }

    @GetMapping("/my")
    public R<PageResult<Map<String, Object>>> mine(@RequestParam(required = false) Integer type,
                                                   @RequestParam(required = false) Integer page,
                                                   @RequestParam(required = false) Integer pageSize) {
        return R.ok(userCenterService.listMyFollows(type, page, pageSize));
    }

    @GetMapping("/{type}/{id}")
    public R<Map<String, Object>> status(@PathVariable("type") Integer type, @PathVariable("id") Long id) {
        PageResult<Map<String, Object>> follows = userCenterService.listMyFollows(type, 1, 1000);
        boolean following = follows.getRecords().stream()
                .anyMatch(item -> id.equals(item.get("followId")));
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("type", type);
        result.put("followId", id);
        result.put("following", following);
        return R.ok(result);
    }

    @PostMapping("/{type}/{id}")
    public R<Map<String, Object>> create(@PathVariable("type") Integer type, @PathVariable("id") Long id) {
        Map<String, Object> result = userCenterService.createFollow(type, id);
        result.put("following", true);
        return R.ok(result);
    }

    @DeleteMapping("/{type}/{id}")
    public R<Object> delete(@PathVariable("type") Integer type, @PathVariable("id") Long id) {
        PageResult<Map<String, Object>> follows = userCenterService.listMyFollows(type, 1, 1000);
        boolean following = follows.getRecords().stream()
                .anyMatch(item -> id.equals(item.get("followId")));
        if (following) {
            userCenterService.deleteFollow(type, id);
        }
        return R.ok();
    }
}
