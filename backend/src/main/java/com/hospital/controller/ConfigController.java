package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/configs")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @GetMapping("/{key}")
    public Result<String> getConfig(@PathVariable String key) {
        String value = configService.getConfigValueByKey(key);
        if (value == null) {
            return Result.fail(404, "配置项不存在");
        }
        return Result.ok(value);
    }
}
