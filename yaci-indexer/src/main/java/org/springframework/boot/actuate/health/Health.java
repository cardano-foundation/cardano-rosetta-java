package org.springframework.boot.actuate.health;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Binary compatibility for the Spring Boot 3 {@code Health} type used by yaci-store.
 * Boot 4's {@code org.springframework.boot.health.contributor.Health} is final, so this is a
 * standalone stand-in with the factory methods yaci-store calls.
 */
public final class Health {

  private final Status status;
  private final Map<String, Object> details;

  private Health(Status status, Map<String, Object> details) {
    this.status = status;
    this.details = details;
  }

  public static Builder unknown() {
    return new Builder().unknown();
  }

  public Status getStatus() {
    return status;
  }

  public Map<String, Object> getDetails() {
    return details;
  }

  public static final class Builder {

    private Status status = Status.UNKNOWN;
    private final Map<String, Object> details = new LinkedHashMap<>();

    public Builder() {
    }

    public Builder withDetail(String key, Object value) {
      details.put(key, value);
      return this;
    }

    public Builder unknown() {
      this.status = Status.UNKNOWN;
      return this;
    }

    public Builder up() {
      this.status = Status.UP;
      return this;
    }

    public Builder down() {
      this.status = Status.DOWN;
      return this;
    }

    public Builder outOfService() {
      this.status = Status.OUT_OF_SERVICE;
      return this;
    }

    public Health build() {
      return new Health(status, Collections.unmodifiableMap(new LinkedHashMap<>(details)));
    }
  }
}
