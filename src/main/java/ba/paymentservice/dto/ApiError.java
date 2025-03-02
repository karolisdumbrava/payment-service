package ba.paymentservice.dto;

import java.time.LocalDateTime;

public record ApiError(
        int errorCode,
        String message,
        String path,
        LocalDateTime timestamp
) {
}
