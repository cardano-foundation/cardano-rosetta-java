package org.cardanofoundation.rosetta.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Word128TypeValidator.class)
@Documented
public @interface Word128Type {
  String message() default "The value must be Word128Type";

  Class<?>[] groups() default { };

  Class<? extends Payload>[] payload() default { };
}

