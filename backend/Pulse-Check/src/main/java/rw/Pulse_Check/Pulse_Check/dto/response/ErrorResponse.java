package rw.Pulse_Check.Pulse_Check.dto.response;

import java.time.Instant;

public record ErrorResponse(
        String error,
        String message,
        Instant timestamp) {
}
