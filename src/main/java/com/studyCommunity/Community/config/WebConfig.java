package com.studyCommunity.Community.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserHeaderInterceptor userHeaderInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userHeaderInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/health",
                        "/alb-health",
                        "/actuator/**",
                        "/error"
                );
    }

    //cors도 설정 해보기
}
