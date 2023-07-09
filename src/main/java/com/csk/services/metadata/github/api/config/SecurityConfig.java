package com.csk.services.metadata.github.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig  {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.csrf()
                        .disable()
                                .cors().disable()
                .authorizeHttpRequests(http -> http.requestMatchers("/login/**").permitAll().anyRequest().authenticated())
                .oauth2Login();

        return httpSecurity.build();
    }
}
