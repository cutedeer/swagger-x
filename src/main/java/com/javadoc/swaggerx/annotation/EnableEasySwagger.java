package com.javadoc.swaggerx.annotation;

import com.javadoc.swaggerx.entity.EasySwaggerConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author shuzhusngsu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Import({EasySwaggerConfig.class})
@Documented
public @interface EnableEasySwagger {
}
