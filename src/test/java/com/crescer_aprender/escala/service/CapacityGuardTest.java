package com.crescer_aprender.escala.service;

import com.crescer_aprender.escala.exception.CapacityViolationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CapacityGuardTest {

    private final CapacityGuard guard = new CapacityGuard();

    @Test
    void testValidate_Success() {
        assertDoesNotThrow(() -> guard.validate(4));
        assertDoesNotThrow(() -> guard.validate(6));
        assertDoesNotThrow(() -> guard.validate(8));
    }

    @Test
    void testValidate_BelowMin() {
        CapacityViolationException e = assertThrows(CapacityViolationException.class, () -> guard.validate(3));
        assertTrue(e.getMessage().contains("mínimo 4"));
    }

    @Test
    void testValidate_AboveMax() {
        CapacityViolationException e = assertThrows(CapacityViolationException.class, () -> guard.validate(9));
        assertTrue(e.getMessage().contains("máximo 8"));
    }
}
