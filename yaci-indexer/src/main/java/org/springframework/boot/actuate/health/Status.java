package org.springframework.boot.actuate.health;

/**
 * Binary compatibility for the Spring Boot 3 health status type.
 */
public final class Status {

  public static final Status UNKNOWN = new Status("UNKNOWN");
  public static final Status UP = new Status("UP");
  public static final Status DOWN = new Status("DOWN");
  public static final Status OUT_OF_SERVICE = new Status("OUT_OF_SERVICE");

  private final String code;

  public Status(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  @Override
  public String toString() {
    return code;
  }
}
