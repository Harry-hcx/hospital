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

    @PostMapping("/hospital/{id}")
    public R<Map<String, Object>> followHospital(@PathVariable("id") Long id) {
        return create(1, id);
    }

    @DeleteMapping("/hospital/{id}")
    public R<Object> unfollowHospital(@PathVariable("id") Long id) {
        return delete(1, id);
    }

    @PostMapping("/doctor/{id}")
    public R<Map<String, Object>> followDoctor(@PathVariable("id") Long id) {
        return create(2, id);
    }

    @DeleteMapping("/doctor/{id}")
    public R<Object> unfollowDoctor(@PathVariable("id") Long id) {
        return delete(2, id);
    }

    @PostMapping("/disease/{id}")
    public R<Map<String, Object>> followDisease(@PathVariable("id") Long id) {
        return create(3, id);
    }

    @DeleteMapping("/disease/{id}")
    public R<Object> unfollowDisease(@PathVariable("id") Long id) {
        return delete(3, id);
    }

    @DeleteMapping("/{type}/{id}")
    public R<Object> delete(@PathVariable("type") Integer type, @PathVariable("id") Long id) {
        userCenterService.deleteFollow(type, id);
        return R.ok();
    }
}
