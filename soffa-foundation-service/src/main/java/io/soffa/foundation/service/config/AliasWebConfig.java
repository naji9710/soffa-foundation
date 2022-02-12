package io.soffa.foundation.service.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
class AliasWebConfig implements WebMvcConfigurer {

    @Value("${app.health.root.enabled:false}")
    private boolean healthRootEnabled;

    @Override
    public void addViewControllers(@NotNull ViewControllerRegistry registry) {
        if (healthRootEnabled) {
            registry.addViewController("/").setViewName("forward:/actuator/health");
        }
        registry.addViewController("/health").setViewName("forward:/actuator/health");
    }

}
