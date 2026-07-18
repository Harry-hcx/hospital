package com.whlg.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.whlg.hospital.dto.ChangePasswordRequest;
import com.whlg.hospital.dto.LoginRequest;
import com.whlg.hospital.dto.RegisterRequest;
import com.whlg.hospital.dto.SendCaptchaRequest;
import com.whlg.hospital.entity.User;
import com.whlg.hospital.entity.UserToken;
import com.whlg.hospital.mapper.UserMapper;
import com.whlg.hospital.mapper.UserTokenMapper;
import com.whlg.hospital.service.AuthService;
import com.whlg.hospital.support.ApiException;
import com.whlg.hospital.support.CurrentUserHolder;
import com.whlg.hospital.util.AliyunSmsUtil;
import com.whlg.hospital.util.PasswordUtil;
import com.whlg.hospital.util.StatusCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class AuthServiceImpl extends ServiceSupport implements AuthService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");
    private static final long CAPTCHA_EXPIRE_SECONDS = 300L;
    private static final long CAPTCHA_COOLDOWN_SECONDS = 60L;
    private static final String CAPTCHA_CODE_KEY_PREFIX = "auth:captcha:code:";
    private static final String CAPTCHA_COOLDOWN_KEY_PREFIX = "auth:captcha:cooldown:";

    private final UserMapper userMapper;
    private final UserTokenMapper userTokenMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final AliyunSmsUtil aliyunSmsUtil;
    private final String smsSignName;
    private final String smsTemplateCode;

    public AuthServiceImpl(UserMapper userMapper,
                           UserTokenMapper userTokenMapper,
                           StringRedisTemplate stringRedisTemplate,
                           AliyunSmsUtil aliyunSmsUtil,
                           @Value("${aliyun.sms.sign-name:}") String smsSignName,
                           @Value("${aliyun.sms.template-code:}") String smsTemplateCode) {
        this.userMapper = userMapper;
        this.userTokenMapper = userTokenMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.aliyunSmsUtil = aliyunSmsUtil;
        this.smsSignName = smsSignName;
        this.smsTemplateCode = smsTemplateCode;
    }

    @Override
    public Map<String, Object> sendCaptcha(SendCaptchaRequest request) {
        check(request != null, "请求体不能为空");
        String phone = normalizePhone(request.getPhone());
        check(PHONE_PATTERN.matcher(phone).matches(), "手机号格式不正确");
        checkNotBlank(smsSignName, "短信签名未配置");
        checkNotBlank(smsTemplateCode, "短信模板未配置");

        String cooldownKey = captchaCooldownKey(phone);
        Boolean allowed = stringRedisTemplate.opsForValue()
                .setIfAbsent(cooldownKey, "1", CAPTCHA_COOLDOWN_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(allowed)) {
            throw new ApiException(StatusCode.BAD_REQUEST, "验证码发送过于频繁，请稍后再试");
        }

        String code = AliyunSmsUtil.generateVerificationCode();
        try {
            String templateParam = String.format("{\"code\":\"%s\",\"min\":\"5\"}", code);
            aliyunSmsUtil.sendSmsVerifyCode(phone, smsSignName, smsTemplateCode, templateParam, "captcha-" + phone);
            stringRedisTemplate.opsForValue().set(captchaCodeKey(phone), code, CAPTCHA_EXPIRE_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ex) {
            stringRedisTemplate.delete(cooldownKey);
            throw new ApiException(StatusCode.ERROR, "验证码发送失败: " + ex.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("phone", phone);
        result.put("expireSeconds", CAPTCHA_EXPIRE_SECONDS);
        result.put("cooldownSeconds", CAPTCHA_COOLDOWN_SECONDS);
        return result;
    }

    @Override
    public Map<String, Object> register(RegisterRequest request) {
        check(request != null, "请求体不能为空");
        String phone = normalizePhone(request.getPhone());
        check(PHONE_PATTERN.matcher(phone).matches(), "手机号格式不正确");
        check(request.getPassword() != null && !request.getPassword().trim().isEmpty(), "密码不能为空");
        check(request.getPassword().equals(request.getConfirmPassword()), "两次密码不一致");
        verifyCaptcha(phone, request.getCaptcha());

        User exists = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (exists != null) {
            throw new ApiException(StatusCode.BAD_REQUEST, "手机号已注册");
        }

        User user = new User();
        user.setUsername(phone);
        user.setPhone(phone);
        user.setPassword(PasswordUtil.md5(request.getPassword()));
        user.setRealName("用户" + phone.substring(phone.length() - 4));
        user.setAvatar("/avatar/default.png");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        stringRedisTemplate.delete(captchaCodeKey(phone));

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("userId", user.getId());
        return result;
    }

    @Override
    public Map<String, Object> login(LoginRequest request) {
        check(request != null, "请求体不能为空");
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        check(user != null && PasswordUtil.matches(request.getPassword(), user.getPassword()), "手机号或密码错误");
        upgradePasswordIfLegacy(user, request.getPassword());

        String token = "token-" + user.getId() + "-" + System.nanoTime();
        UserToken userToken = new UserToken();
        userToken.setUserId(user.getId());
        userToken.setToken(token);
        userToken.setStatus(1);
        userToken.setExpireTime(LocalDateTime.now().plusDays(7));
        userToken.setCreateTime(LocalDateTime.now());
        userToken.setUpdateTime(LocalDateTime.now());
        userTokenMapper.insert(userToken);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("token", token);
        result.put("userInfo", buildUserInfo(user));
        return result;
    }

    @Override
    public Map<String, Object> me() {
        User user = userMapper.selectById(requireUserId());
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("userInfo", buildUserInfo(user));
        return result;
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        Long userId = requireUserId();
        User user = userMapper.selectById(userId);
        check(PasswordUtil.matches(request.getOldPassword(), user.getPassword()), "旧密码错误");
        check(request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty(), "新密码不能为空");
        check(request.getNewPassword().equals(request.getConfirmPassword()), "两次密码不一致");
        user.setPassword(PasswordUtil.md5(request.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        disableUserTokens(userId);
    }

    @Override
    public void logout(String authorization) {
        check(authorization != null && authorization.startsWith("Bearer "), "未登录");
        String token = authorization.substring("Bearer ".length()).trim();
        userTokenMapper.update(null, new LambdaUpdateWrapper<UserToken>()
                .eq(UserToken::getToken, token)
                .set(UserToken::getStatus, 0)
                .set(UserToken::getUpdateTime, LocalDateTime.now()));
        CurrentUserHolder.clear();
    }

    private void verifyCaptcha(String phone, String captcha) {
        check(captcha != null && !captcha.trim().isEmpty(), "验证码不能为空");
        String cachedCaptcha = stringRedisTemplate.opsForValue().get(captchaCodeKey(phone));
        check(cachedCaptcha != null, "验证码已过期，请重新获取");
        check(cachedCaptcha.equals(captcha.trim()), "验证码错误");
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.trim();
    }

    private String captchaCodeKey(String phone) {
        return CAPTCHA_CODE_KEY_PREFIX + phone;
    }

    private String captchaCooldownKey(String phone) {
        return CAPTCHA_COOLDOWN_KEY_PREFIX + phone;
    }

    private void checkNotBlank(String value, String message) {
        check(value != null && !value.trim().isEmpty(), message);
    }

    private void disableUserTokens(Long userId) {
        userTokenMapper.update(null, new LambdaUpdateWrapper<UserToken>()
                .eq(UserToken::getUserId, userId)
                .eq(UserToken::getStatus, 1)
                .set(UserToken::getStatus, 0)
                .set(UserToken::getUpdateTime, LocalDateTime.now()));
    }

    private void upgradePasswordIfLegacy(User user, String rawPassword) {
        if (user == null || rawPassword == null || user.getPassword() == null) {
            return;
        }
        if (!user.getPassword().equals(rawPassword)) {
            return;
        }
        user.setPassword(PasswordUtil.md5(rawPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> userInfo = new LinkedHashMap<String, Object>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getRealName());
        userInfo.put("phone", user.getPhone());
        userInfo.put("avatar", user.getAvatar());
        return userInfo;
    }
}