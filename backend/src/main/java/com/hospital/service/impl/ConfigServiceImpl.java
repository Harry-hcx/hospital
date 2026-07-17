package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.entity.Config;
import com.hospital.mapper.ConfigMapper;
import com.hospital.service.ConfigService;
import org.springframework.stereotype.Service;

@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {

    @Override
    public String getConfigValueByKey(String key) {
        Config config = getOne(new LambdaQueryWrapper<Config>().eq(Config::getConfigKey, key));
        return config != null ? config.getConfigValue() : null;
    }
}
