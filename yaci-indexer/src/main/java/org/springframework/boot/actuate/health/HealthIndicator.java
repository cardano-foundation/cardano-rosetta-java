package org.springframework.boot.actuate.health;

/**
 * Binary compatibility for libraries compiled against Spring Boot 3, including yaci-store 2.0.x.
 * Spring Boot 4 moved this type to {@code org.springframework.boot.health.contributor.HealthIndicator}.
 */
public interface HealthIndicator {

  Health health();
}
