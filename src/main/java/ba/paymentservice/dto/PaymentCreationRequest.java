package ba.paymentservice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PaymentCreationRequest(
        @NotNull(message = "Payment type is required")
        PaymentType paymentType,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        // Example
        @NotNull(message = "Currency is required")
        Currency currency, // "EUR" or "USD"

        @NotBlank(message = "Debtor IBAN is required")
        @Size(max = 34, message = "Debtor IBAN must be at most 34 characters")
        String debtorIban,

        @NotBlank(message = "Creditor IBAN is required")
        @Size(min = 15, max = 34, message = "IBAN must be between 15 and 34 characters")
        @Pattern(regexp = "^[A-Z]{2}\\d{2}[A-Z0-9]{11,30}$", message = "Invalid IBAN format")
        String creditorIban,

        // Type 1 and Type 2 payments require details
        String details,

        // Type 3 payments require creditor bank BIC
        @Size(max = 11)
        String creditorBankBic,

        @NotNull(message = "User ID is required")
        Long userId
) {
}