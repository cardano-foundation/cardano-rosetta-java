package org.cardanofoundation.rosetta.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Hash28TypeValidator.class)
@Documented
public @interface Hash28Type {
  String message() default "The value must be Hash28Type";

  Class<?>[] groups() default { };

  Class<? extends Payload>[] payload() default { };
}

