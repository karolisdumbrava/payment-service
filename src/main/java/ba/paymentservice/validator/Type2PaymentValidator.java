package ba.paymentservice.validator;

import ba.paymentservice.dto.Currency;
import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.exception.BadRequestException;

public class Type2PaymentValidator implements PaymentValidator {
    @Override
    public void validate(PaymentCreationRequest request) {
        Currency currency = Currency.valueOf(request.getCurrency().toUpperCase());
        if (currency != Currency.USD) {
            throw new BadRequestException("Currency must be USD for payment TYPE2");
        }
    }
}
