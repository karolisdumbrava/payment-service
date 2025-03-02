package ba.paymentservice.dto;

import java.math.BigDecimal;

public record PaymentCancellationResponse(Long id, BigDecimal cancellationFee) {
    public long getId() {
        return id;
    }

    public BigDecimal getCancellationFee() {
        return cancellationFee;
    }
}
