package ba.paymentservice.validator;

import ba.paymentservice.dto.Currency;
import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.dto.PaymentType;
import ba.paymentservice.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class Type2PaymentValidator implements PaymentValidator {

    @Override
    public PaymentType getSupportedPaymentType() {
        return PaymentType.TYPE2;
    }

    @Override
    public void validate(PaymentCreationRequest request) {
        Currency currency = request.currency();
        if (currency != Currency.USD) {
            throw new BadRequestException("Currency must be USD for payment TYPE2");
        }
    }
}
