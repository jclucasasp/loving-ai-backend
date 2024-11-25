package com.example.ai_dating_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Configuration
@EnableCaching
public class WebConfig implements WebMvcConfigurer {

    private final ResourceLoader resourceLoader;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            String location = this.resourceLoader.getResource("file:images/").getURI().toString();

            registry.addResourceHandler("/images/**")
                    .addResourceLocations(location)
                    .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
