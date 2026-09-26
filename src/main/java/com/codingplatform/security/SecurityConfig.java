package com.codingplatform.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.codingplatform.fluxguard.filter.FluxGuardRequestProtectionFilter;
import com.codingplatform.fluxguard.service.FluxGuardLoggingService;
import com.codingplatform.fluxguard.service.RequestDeduplicationStore;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final FluxGuardLoggingService fluxGuardLoggingService;
    private final RequestDeduplicationStore requestDeduplicationStore;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                          FluxGuardLoggingService fluxGuardLoggingService,
                          RequestDeduplicationStore requestDeduplicationStore) {
        this.customUserDetailsService = customUserDetailsService;
        this.fluxGuardLoggingService = fluxGuardLoggingService;
        this.requestDeduplicationStore = requestDeduplicationStore;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .successHandler((request, response, authentication) -> {
                        fluxGuardLoggingService.recordAuthenticationEvent(request, HttpServletResponse.SC_OK,
                                    null, authentication);
                            response.sendRedirect("/dashboard");
                        })
                        .failureHandler((request, response, exception) -> {
                        fluxGuardLoggingService.recordAuthenticationEvent(request, HttpServletResponse.SC_UNAUTHORIZED,
                                    "Invalid credentials", null);
                            response.sendRedirect("/login?error");
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        http.addFilterBefore(new FluxGuardRequestProtectionFilter(fluxGuardLoggingService, requestDeduplicationStore),
            UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}