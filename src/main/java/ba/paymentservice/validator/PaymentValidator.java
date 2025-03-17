package ba.paymentservice.validator;

import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.dto.PaymentType;

public interface PaymentValidator {
    PaymentType getSupportedPaymentType();
    void validate(PaymentCreationRequest request);
}
