package com.spaziocodice.labs.fraglink;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ComponentScan("com.spaziocodice.labs.fraglink")
public class WebConfiguration implements WebMvcConfigurer {
    @Bean
    public FragLinkAdvertiser advertiser() {
        return new FragLinkAdvertiser();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedMethods("*");
    }
}