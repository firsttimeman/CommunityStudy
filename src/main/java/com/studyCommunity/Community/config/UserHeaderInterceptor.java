package com.studyCommunity.Community.config;

import com.studyCommunity.Community.exception.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserHeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("X-User-Id header is required");
        }
        request.setAttribute("userId", userId);
        return true;
    }

    /**
     인터셉터로 매번 컨트롤러에서 요청을 보내면 번거로울 뿐만 아니라 만약 x-user-id형식이 아닌 다른 형식으로 변경될 경우
     편하게 변경이 가능
     *
     */

}
