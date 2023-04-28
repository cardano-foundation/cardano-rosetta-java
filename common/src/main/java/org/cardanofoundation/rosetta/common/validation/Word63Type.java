package org.cardanofoundation.rosetta.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Word63TypeValidator.class)
@Documented
public @interface Word63Type {
  String message() default "The value must be Word63Type";

  Class<?>[] groups() default { };

  Class<? extends Payload>[] payload() default { };
}
