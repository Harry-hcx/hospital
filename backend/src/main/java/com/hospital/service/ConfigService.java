package com.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.entity.Config;

public interface ConfigService extends IService<Config> {

    String getConfigValueByKey(String key);
}
