package com.whlg.hospital.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whlg.hospital.entity.UserToken;
import com.whlg.hospital.mapper.UserTokenMapper;
import com.whlg.hospital.util.StatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Set<String> EXCLUDE_PATHS = new HashSet<String>(Arrays.asList(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/sendCaptcha",
            "/api/home/index",
            "/api/hospitals",
            "/api/departments/tree",
            "/api/departments/primary",
            "/api/doctors",
            "/api/diseases",
            "/api/articles",
            "/api/search/global",
            "/api/health",
            "/api/payments/callback",
            "/api/payments/alipay/notify",
            "/api/payments/alipay/return",
            "/api/docs"
    ));

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final UserTokenMapper userTokenMapper;

    public AuthInterceptor(UserTokenMapper userTokenMapper) {
        this.userTokenMapper = userTokenMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestUri = request.getRequestURI();
        if (isPublic(requestUri, request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new ApiException(StatusCode.UNAUTHORIZED, "未登录"+requestUri);
        }

        String token = authorization.substring("Bearer ".length()).trim();
        UserToken userToken = userTokenMapper.selectOne(new LambdaQueryWrapper<UserToken>()
                .eq(UserToken::getToken, token)
                .eq(UserToken::getStatus, 1));
        if (userToken == null || (userToken.getExpireTime() != null && userToken.getExpireTime().isBefore(LocalDateTime.now()))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new ApiException(StatusCode.UNAUTHORIZED, "登录已失效");
        }

        CurrentUserHolder.set(userToken.getUserId());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentUserHolder.clear();
    }

    private boolean isPublic(String requestUri, String method) {
        if ("OPTIONS".equalsIgnoreCase(method) || EXCLUDE_PATHS.contains(requestUri)) {
            return true;
        }
        return "GET".equalsIgnoreCase(method) && (
                pathMatcher.match("/api/hospitals/*", requestUri)
                        || pathMatcher.match("/api/hospitals/*/departments", requestUri)
                        || pathMatcher.match("/api/hospitals/*/doctors", requestUri)
                        || pathMatcher.match("/api/departments/*/children", requestUri)
                        || pathMatcher.match("/api/doctors/*", requestUri)
                        || pathMatcher.match("/api/doctors/*/schedules", requestUri)
                        || pathMatcher.match("/api/doctors/*/reviews", requestUri)
                        || pathMatcher.match("/api/diseases/*", requestUri)
                        || pathMatcher.match("/api/articles/*", requestUri)
                        || pathMatcher.match("/api/configs/*", requestUri)
        );
    }
}
