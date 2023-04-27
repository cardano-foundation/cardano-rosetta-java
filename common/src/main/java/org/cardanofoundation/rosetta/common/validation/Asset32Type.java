package org.cardanofoundation.rosetta.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Asset32TypeValidator.class)
@Documented
public @interface Asset32Type {
  String message() default "The value must be Asset32Type";

  Class<?>[] groups() default { };

  Class<? extends Payload>[] payload() default { };
}

