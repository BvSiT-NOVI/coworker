package nl.bvsit.coworker.customvalidator;

import nl.bvsit.coworker.payload.CwSessionOrderDTO;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.Set;

//See https://www.baeldung.com/spring-mvc-custom-validator
public class SizeOrdersConstraintValidator implements ConstraintValidator<SizeOrdersConstraint, Collection<CwSessionOrderDTO>> {

    private int set_size;

    @Override
    public void initialize(SizeOrdersConstraint constraintAnnotation) {
        set_size = constraintAnnotation.set_size();
    }

    @Override
    public boolean isValid(Collection<CwSessionOrderDTO> values, ConstraintValidatorContext constraintValidatorContext) {
        if (values.size() == set_size) return true;
        //https://docs.jboss.org/hibernate/validator/4.1/reference/en-US/html/validator-customconstraints.html#validator-customconstraints-validator
        constraintValidatorContext.disableDefaultConstraintViolation();
        String defaultMessage=constraintValidatorContext.getDefaultConstraintMessageTemplate();
        String message = String.format(defaultMessage,set_size);
        constraintValidatorContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }

}
