package org.springframework.boot.autoconfigure.domain;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.persistence.autoconfigure.EntityScanPackages;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Forwards Boot 3 {@link EntityScan} packages to Boot 4 {@link EntityScanPackages}.
 */
public class LegacyEntityScanPackagesRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

  private Environment environment;

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Override
  public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
    AnnotationAttributes attributes = AnnotationAttributes.fromMap(
        metadata.getAnnotationAttributes(EntityScan.class.getName()));
    Set<String> packages = new LinkedHashSet<>();
    if (attributes != null) {
      for (String basePackage : attributes.getStringArray("basePackages")) {
        String[] tokens = StringUtils.tokenizeToStringArray(
            environment.resolvePlaceholders(basePackage), ",; \t\n");
        Collections.addAll(packages, tokens);
      }
      for (Class<?> type : attributes.getClassArray("basePackageClasses")) {
        packages.add(ClassUtils.getPackageName(type));
      }
    }
    if (packages.isEmpty()) {
      packages.add(ClassUtils.getPackageName(metadata.getClassName()));
    }
    EntityScanPackages.register(registry, packages);
  }
}
