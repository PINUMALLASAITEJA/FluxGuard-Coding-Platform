package com.codingplatform.fluxguard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.codingplatform.fluxguard.interceptor.FluxGuardRequestLoggingInterceptor;

@Configuration
public class FluxGuardWebConfig implements WebMvcConfigurer {

    private final FluxGuardRequestLoggingInterceptor fluxGuardRequestLoggingInterceptor;

    public FluxGuardWebConfig(FluxGuardRequestLoggingInterceptor fluxGuardRequestLoggingInterceptor) {
        this.fluxGuardRequestLoggingInterceptor = fluxGuardRequestLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(fluxGuardRequestLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/",
                        "/login",
                        "/register",
                        "/logout",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/error"
                );
    }
}
