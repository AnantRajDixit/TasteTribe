package com.tastetribe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration. The React frontend talks to this API through the Vite proxy
 * (same origin), but the preview host and direct tooling (Postman) benefit from an
 * explicit credentialed CORS policy. {@code allowedOriginPatterns} is used so the
 * wildcard can coexist with {@code allowCredentials(true)}.
 *
 * <p>Also publishes the uploads directory so stored cover photos are servable at
 * {@code /api/uploads/**}.</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String allowedOrigins;
    private final String uploadsDir;

    public WebConfig(
            @org.springframework.beans.factory.annotation.Value("${app.cors.origins:*}") String allowedOrigins,
            @org.springframework.beans.factory.annotation.Value("${app.uploads.dir:./uploads}") String uploadsDir) {
        this.allowedOrigins = allowedOrigins;
        this.uploadsDir = uploadsDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = java.nio.file.Paths.get(uploadsDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(604800);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
