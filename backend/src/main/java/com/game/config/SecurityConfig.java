package com.game.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Spring Security配置
 * 开发环境：允许所有请求（不进行认证）
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（开发环境）
            .csrf().disable()
            // 允许所有请求，包括H2控制台
            .authorizeRequests()
                .antMatchers("/h2-console/**").permitAll()  // 明确允许H2控制台
                .anyRequest().permitAll()
            .and()
            // 允许H2控制台的frame（H2控制台需要frame支持）
            .headers().frameOptions().sameOrigin();
    }
}

