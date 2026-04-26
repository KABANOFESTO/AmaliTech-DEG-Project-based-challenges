package rw.idempotency_gateway.idempotency_gateway.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PaymentRequest(
        @NotNull(message = "Amount is required.")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero.")
        @Digits(integer = 12, fraction = 2, message = "Amount must have up to 12 digits and 2 decimal places.")
        BigDecimal amount,

        @NotBlank(message = "Currency is required.")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code.")
        String currency) {

    public String normalizedCurrency() {
        return currency == null ? null : currency.trim().toUpperCase();
    }

    public String chargeMessage() {
        return "Charged " + amount.stripTrailingZeros().toPlainString() + " " + normalizedCurrency();
    }
}
