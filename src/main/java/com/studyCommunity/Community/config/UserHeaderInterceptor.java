package com.studyCommunity.Community.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.web.servlet.HandlerInterceptor;

public class UserHeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader("X-User-Id");
        if(userId == null || userId.isBlank()) {
            throw new BadRequestException("X-User-Id header is required");
        }
        request.setAttribute("userId", userId);
        return true;
    }
}
