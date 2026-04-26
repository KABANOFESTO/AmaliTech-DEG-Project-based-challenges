package rw.Pulse_Check.Pulse_Check.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record MonitorRegistrationRequest(
        @NotBlank(message = "Monitor id is required.")
        String id,

        @Min(value = 1, message = "Timeout must be greater than zero.")
        long timeout,

        @JsonProperty("alert_email")
        @NotBlank(message = "Alert email is required.")
        @Email(message = "Alert email must be a valid email address.")
        String alertEmail) {
}
