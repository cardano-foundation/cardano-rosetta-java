package org.cardanofoundation.rosetta.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Word64TypeValidator.class)
@Documented
public @interface Word64Type {
  String message() default "The value must be Word64Type";

  Class<?>[] groups() default { };

  Class<? extends Payload>[] payload() default { };
}

