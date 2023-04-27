package org.cardanofoundation.rosetta.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Hash32TypeValidator.class)
@Documented
public @interface Hash32Type {
  String message() default "The value must be Hash32Type";

  Class<?>[] groups() default { };

  Class<? extends Payload>[] payload() default { };
}

