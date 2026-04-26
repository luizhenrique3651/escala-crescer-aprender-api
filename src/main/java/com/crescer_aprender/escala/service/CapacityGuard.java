package com.crescer_aprender.escala.service;

import com.crescer_aprender.escala.exception.CapacityViolationException;
import org.springframework.stereotype.Component;

@Component
public class CapacityGuard {

    private static final int MIN_VOLUNTEERS = 4;
    private static final int MAX_VOLUNTEERS = 8;

    public void validate(int count) {
        if (count < MIN_VOLUNTEERS) {
            throw new CapacityViolationException("A escala deve ter no mínimo " + MIN_VOLUNTEERS + " voluntários.");
        }
        if (count > MAX_VOLUNTEERS) {
            throw new CapacityViolationException("A escala deve ter no máximo " + MAX_VOLUNTEERS + " voluntários.");
        }
    }
}
