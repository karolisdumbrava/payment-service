package ba.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCreationRequest(
        @NotBlank String username
) {
}
