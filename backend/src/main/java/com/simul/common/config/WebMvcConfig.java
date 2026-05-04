package com.simul.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String uploadPath;
    private final String urlPrefix;

    public WebMvcConfig(
            @Value("${simul.storage.local.path:./uploads/images/}") String uploadPath,
            @Value("${simul.storage.local.url-prefix:/uploads/images/}") String urlPrefix) {
        this.uploadPath = uploadPath;
        this.urlPrefix = urlPrefix;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String pattern = urlPrefix.endsWith("/") ? urlPrefix + "**" : urlPrefix + "/**";
        String absoluteUploadPath = Paths.get(uploadPath).toAbsolutePath().toUri().toString();
        
        registry.addResourceHandler(pattern)
                .addResourceLocations(absoluteUploadPath);
    }
}
