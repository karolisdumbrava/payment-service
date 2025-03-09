package ba.paymentservice.service;

import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.dto.PaymentType;
import ba.paymentservice.exception.BadRequestException;
import ba.paymentservice.validator.*;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
public class PaymentValidationService {
    private final Map<PaymentType, PaymentValidator> validatorMap;

    public PaymentValidationService() {
        validatorMap = new EnumMap<>(PaymentType.class);
        validatorMap.put(PaymentType.TYPE1, new Type1PaymentValidator());
        validatorMap.put(PaymentType.TYPE2, new Type2PaymentValidator());
        validatorMap.put(PaymentType.TYPE3, new Type3PaymentValidator());
    }

    public void validate(PaymentCreationRequest request) {
        PaymentValidator validator = validatorMap.get(request.paymentType());
        if (validator == null) {
            throw new BadRequestException("Invalid payment type");
        }
        validator.validate(request);
    }
}
