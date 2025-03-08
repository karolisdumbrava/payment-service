package ba.paymentservice.validator;

import ba.paymentservice.dto.PaymentCreationRequest;

public interface PaymentValidator {
    void validate(PaymentCreationRequest request);
}
