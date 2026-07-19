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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.security.SecureRandom;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl extends ServiceSupport implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");
    private static final long CAPTCHA_EXPIRE_SECONDS = 300L;
    private static final long CAPTCHA_COOLDOWN_SECONDS = 60L;
    private static final String CAPTCHA_CODE_KEY_PREFIX = "auth:captcha:code:";
    private static final String CAPTCHA_COOLDOWN_KEY_PREFIX = "auth:captcha:cooldown:";
    private static final SecureRandom TOKEN_RANDOM = new SecureRandom();

    private final UserMapper userMapper;
    private final UserTokenMapper userTokenMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final AliyunSmsUtil aliyunSmsUtil;
    private final Environment environment;
    private final String smsSignName;
    private final String smsTemplateCode;
    private final boolean captchaBypassEnabled;
    private final String captchaBypassCode;
    private final Set<String> captchaBypassPhones;

    public AuthServiceImpl(UserMapper userMapper,
                           UserTokenMapper userTokenMapper,
                           StringRedisTemplate stringRedisTemplate,
                           AliyunSmsUtil aliyunSmsUtil,
                           Environment environment,
                           @Value("${aliyun.sms.sign-name:}") String smsSignName,
                           @Value("${aliyun.sms.template-code:}") String smsTemplateCode,
                           @Value("${auth.captcha-bypass.enabled:false}") boolean captchaBypassEnabled,
                           @Value("${auth.captcha-bypass.code:}") String captchaBypassCode,
                           @Value("${auth.captcha-bypass.phones:}") String captchaBypassPhones) {
        this.userMapper = userMapper;
        this.userTokenMapper = userTokenMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.aliyunSmsUtil = aliyunSmsUtil;
        this.environment = environment;
        this.smsSignName = smsSignName;
        this.smsTemplateCode = smsTemplateCode;
        this.captchaBypassEnabled = captchaBypassEnabled;
        this.captchaBypassCode = captchaBypassCode == null ? "" : captchaBypassCode.trim();
        this.captchaBypassPhones = Arrays.stream(captchaBypassPhones.split(","))
                .map(String::trim)
                .filter(phone -> !phone.isEmpty())
                .collect(Collectors.toSet());
    }

    @Override
    public Map<String, Object> sendCaptcha(SendCaptchaRequest request) {
        check(request != null, "请求体不能为空");
        String phone = normalizePhone(request.getPhone());
        check(PHONE_PATTERN.matcher(phone).matches(), "手机号格式不正确");
        checkNotBlank(smsSignName, "短信签名未配置");
        checkNotBlank(smsTemplateCode, "短信模板未配置");

        log.info("Sending captcha to phone={}", phone);
        String cooldownKey = captchaCooldownKey(phone);
        Boolean allowed = stringRedisTemplate.opsForValue()
                .setIfAbsent(cooldownKey, "1", CAPTCHA_COOLDOWN_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(allowed)) {
            throw new ApiException(StatusCode.BAD_REQUEST, "验证码发送过于频繁，请稍后再试");
        }

        String code = AliyunSmsUtil.generateVerificationCode();
        try {
            String templateParam = String.format("{\"code\":\"%s\",\"min\":\"5\"}", code);
            String providerResponse = aliyunSmsUtil.sendSmsVerifyCode(phone, smsSignName, smsTemplateCode, templateParam, "captcha-" + phone);
            stringRedisTemplate.opsForValue().set(captchaCodeKey(phone), code, CAPTCHA_EXPIRE_SECONDS, TimeUnit.SECONDS);
            log.info("Captcha sent successfully to phone={}, providerResponse={}", phone, providerResponse);
        } catch (Exception ex) {
            stringRedisTemplate.delete(cooldownKey);
            log.error("Failed to send captcha to phone={}: {}", phone, ex.getMessage(), ex);
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
        String username = request.getUsername() == null ? null : request.getUsername().trim();
        check(username != null && !username.isEmpty(), "用户名不能为空");
        check(PHONE_PATTERN.matcher(phone).matches(), "手机号格式不正确");
        check(request.getPassword() != null && !request.getPassword().trim().isEmpty(), "密码不能为空");

        check(userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, phone)) == 0, "手机号已注册");
        check(userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) == 0, "用户名已存在");

        User user = new User();
        user.setUsername(username);
        user.setPhone(phone);
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setRealName(request.getRealName() == null || request.getRealName().trim().isEmpty()
                ? "用户" + phone.substring(phone.length() - 4)
                : request.getRealName().trim());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        user.setAvatar("/avatar/default.png");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("userId", user.getId());
        return result;
    }

    @Override
    public Map<String, Object> login(LoginRequest request) {
        check(request != null, "请求体不能为空");
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        check(user != null && Integer.valueOf(1).equals(user.getStatus())
                && PasswordUtil.matches(request.getPassword(), user.getPassword()), "手机号或密码错误");
        if (!PasswordUtil.isEncoded(user.getPassword())) {
            user.setPassword(PasswordUtil.encode(request.getPassword()));
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }

        byte[] tokenBytes = new byte[32];
        TOKEN_RANDOM.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
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
        return buildUserInfo(user);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        Long userId = requireUserId();
        User user = userMapper.selectById(userId);
        check(PasswordUtil.matches(request.getOldPassword(), user.getPassword()), "旧密码错误");
        check(request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty(), "新密码不能为空");
        user.setPassword(PasswordUtil.encode(request.getNewPassword()));
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
        String normalizedCaptcha = captcha == null ? "" : captcha.trim();
        check(!normalizedCaptcha.isEmpty(), "验证码不能为空");
        if (isCaptchaBypassAllowed(phone, normalizedCaptcha)) {
            log.warn("Captcha bypass applied for phone={} under profiles={}", phone, Arrays.toString(environment.getActiveProfiles()));
            return;
        }

        String cachedCaptcha = stringRedisTemplate.opsForValue().get(captchaCodeKey(phone));
        check(cachedCaptcha != null, "验证码已过期，请重新获取");
        check(cachedCaptcha.equals(normalizedCaptcha), "验证码错误");
    }

    private boolean isCaptchaBypassAllowed(String phone, String captcha) {
        if (!captchaBypassEnabled) {
            return false;
        }
        if (!environment.acceptsProfiles(Profiles.of("dev", "test", "local"))) {
            return false;
        }
        if (captchaBypassCode.isEmpty() || !captchaBypassCode.equals(captcha)) {
            return false;
        }
        return captchaBypassPhones.isEmpty() || captchaBypassPhones.contains(phone);
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

    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> userInfo = new LinkedHashMap<String, Object>();
        userInfo.put("id", user.getId());
        userInfo.put("realName", user.getRealName());
        userInfo.put("phone", user.getPhone());
        userInfo.put("email", user.getEmail());
        userInfo.put("gender", user.getGender());
        userInfo.put("avatar", user.getAvatar());
        return userInfo;
    }
}
