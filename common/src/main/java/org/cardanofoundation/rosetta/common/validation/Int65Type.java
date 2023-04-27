package org.cardanofoundation.rosetta.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Int65TypeValidator.class)
@Documented
public @interface Int65Type {
  String message() default "The value must be Int65Type";

  Class<?>[] groups() default { };

  Class<? extends Payload>[] payload() default { };
}
