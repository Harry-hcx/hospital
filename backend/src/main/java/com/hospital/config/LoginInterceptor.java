package com.hospital.config;

import com.hospital.auth.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }
        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            response.setStatus(401);
            return false;
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        request.setAttribute("userId", userId);
        return true;
    }
}
