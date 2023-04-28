package org.cardanofoundation.rosetta.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LovelaceValidator.class)
@Documented
public @interface Lovelace {
  String message() default "The value must be Lovelace";

  Class<?>[] groups() default { };

  Class<? extends Payload>[] payload() default { };
}

