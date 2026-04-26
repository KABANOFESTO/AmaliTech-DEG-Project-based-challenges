package rw.Pulse_Check.Pulse_Check.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MonitorActionResponse(
        String message,
        @JsonProperty("monitor")
        MonitorResponse monitor) {
}
