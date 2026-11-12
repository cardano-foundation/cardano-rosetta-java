package org.springframework.boot.autoconfigure.domain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

/**
 * Binary compatibility for {@code @EntityScan} as published with Spring Boot 3.
 * Boot 4 moved the annotation to {@code org.springframework.boot.persistence.autoconfigure.EntityScan}.
 * yaci-store 2.0.x and 3.0.0-beta5 still reference this type.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LegacyEntityScanPackagesRegistrar.class)
public @interface EntityScan {

  @AliasFor("basePackages")
  String[] value() default {};

  @AliasFor("value")
  String[] basePackages() default {};

  Class<?>[] basePackageClasses() default {};
}
