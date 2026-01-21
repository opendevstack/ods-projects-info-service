package org.opendevstack.projects_info_service.server.annotations;

import org.opendevstack.projects_info_service.configuration.CacheConfiguration;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheableWithFallback {
    String primary();
    String fallback();
    String defaultValue() default "";  // SpEL or literal string
    String cacheManager() default CacheConfiguration.CUSTOM_CACHE_MANAGER_NAME;
}
