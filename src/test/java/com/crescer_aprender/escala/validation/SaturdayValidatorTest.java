package com.crescer_aprender.escala.validation;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SaturdayValidatorTest {

    private final SaturdayValidator validator = new SaturdayValidator();

    @Test
    void testIsValid_LocalDate_Success() {
        LocalDate saturday = LocalDate.of(2025, 5, 10);
        assertTrue(validator.isValid(saturday, null));
    }

    @Test
    void testIsValid_LocalDate_Failure() {
        LocalDate sunday = LocalDate.of(2025, 5, 11);
        assertFalse(validator.isValid(sunday, null));
    }

    @Test
    void testIsValid_Collection_Success() {
        List<LocalDate> saturdays = List.of(
            LocalDate.of(2025, 5, 10),
            LocalDate.of(2025, 5, 17)
        );
        assertTrue(validator.isValid(saturdays, null));
    }

    @Test
    void testIsValid_Collection_Failure() {
        List<LocalDate> mixed = List.of(
            LocalDate.of(2025, 5, 10),
            LocalDate.of(2025, 5, 11) // Domingo
        );
        assertFalse(validator.isValid(mixed, null));
    }

    @Test
    void testIsValid_Null() {
        assertTrue(validator.isValid(null, null));
    }
}
