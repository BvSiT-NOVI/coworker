package nl.bvsit.coworker.customvalidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

//TODO generalize to all collection types ?

@Constraint(validatedBy = SizeOrdersConstraintValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface SizeOrdersConstraint {
    int set_size() default 0;
    String message() default "Illegal number of orders. Must be exactly %d";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
