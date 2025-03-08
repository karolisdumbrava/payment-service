package ba.paymentservice.service;

import ba.paymentservice.dto.Currency;
import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.dto.PaymentType;
import ba.paymentservice.exception.BadRequestException;
import ba.paymentservice.validator.*;

import java.util.EnumMap;
import java.util.Map;

public class PaymentValidationService {
    private final Map<PaymentType, PaymentValidator> validatorMap;

    public PaymentValidationService() {
        validatorMap = new EnumMap<>(PaymentType.class);
        validatorMap.put(PaymentType.TYPE1, new Type1PaymentValidator());
        validatorMap.put(PaymentType.TYPE2, new Type2PaymentValidator());
        validatorMap.put(PaymentType.TYPE3, new Type3PaymentValidator());
    }

    public void validate(PaymentCreationRequest request) {
        // Check if currency is in available list of enums
        Currency currency = CurrencyValidator.validateCurrency(request.getCurrency());

        PaymentValidator validator = validatorMap.get(request.getPaymentType());
        if (validator == null) {
            throw new BadRequestException("Invalid payment type");
        }
        validator.validate(request);
    }
}
