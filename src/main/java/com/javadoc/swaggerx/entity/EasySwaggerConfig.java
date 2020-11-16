package com.javadoc.swaggerx.entity;

import com.javadoc.swaggerx.core.Swagger2Hook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.context.WebApplicationContext;
import springfox.documentation.spring.web.DocumentationCache;

import javax.annotation.Resource;

/**
 * @author shuzhusngsu
 */
@Configuration
public class EasySwaggerConfig {

    @Resource
    @Lazy
    private DocumentationCache documentationCache;
    @Resource
    @Lazy
    private WebApplicationContext applicationContext;

    public EasySwaggerConfig() {
    }

    @Bean
    Swagger2Hook provideInit() {
        return new Swagger2Hook(documentationCache, applicationContext);
    }
}
