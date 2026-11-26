package com.lab.emp.config;

import com.lab.emp.interceptor.LoginInterceptor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 子容器。当前只扫描 controller。
 * 全局异常处理类位于 common 包，任务 6 会把该包纳入本容器，否则 {@code @RestControllerAdvice} 不会生效。
 */
@Configuration
@EnableWebMvc
@ComponentScan("com.lab.emp.controller")
public class SpringMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/register",
                        "/",
                        "/index.html",
                        "/static/**",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/**/*.ico",
                        "/**/*.png");
    }
}
