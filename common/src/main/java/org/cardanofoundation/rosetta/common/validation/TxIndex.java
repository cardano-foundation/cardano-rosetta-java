package org.cardanofoundation.rosetta.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TxIndexValidator.class)
@Documented
public @interface TxIndex {
  String message() default "The value must be TxIndex";

  Class<?>[] groups() default { };

  Class<? extends Payload>[] payload() default { };
}

