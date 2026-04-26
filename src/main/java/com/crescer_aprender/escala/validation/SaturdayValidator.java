package com.crescer_aprender.escala.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;

public class SaturdayValidator implements ConstraintValidator<IsSaturday, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return switch (value) {
            case LocalDate date -> date.getDayOfWeek() == DayOfWeek.SATURDAY;
            case Collection<?> list -> list.stream()
                .filter(item -> item instanceof LocalDate)
                .map(item -> (LocalDate) item)
                .allMatch(date -> date.getDayOfWeek() == DayOfWeek.SATURDAY);
            default -> false;
        };
    }
}
