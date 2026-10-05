package com.saasweb.ecommerce.dto;

import com.saasweb.ecommerce.model.Shift;

import java.time.Instant;

public final class ShiftDtos {

    private ShiftDtos() {}

    public record ShiftResponse(String id, String userDni, String userName, Instant openedAt, Instant closedAt) {
        public static ShiftResponse from(Shift s) {
            return new ShiftResponse(s.getId(), s.getUserDni(), s.getUserName(), s.getOpenedAt(), s.getClosedAt());
        }
    }
}
