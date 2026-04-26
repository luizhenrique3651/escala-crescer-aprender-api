package com.crescer_aprender.escala.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SaturdayValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface IsSaturday {
    String message() default "A data deve ser obrigatoriamente um sábado.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
