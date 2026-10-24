package com.app.financas.config;

import com.app.financas.config.AutenticacaoInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AutenticacaoInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/login", "/api/cadastro");
    }
}