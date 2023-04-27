package org.cardanofoundation.rosetta.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Addr29TypeValidator.class)
@Documented
public @interface Addr29Type {
  String message() default "The value must be addr29type";

  Class<?>[] groups() default { };

  Class<? extends Payload>[] payload() default { };
}

