package ba.paymentservice.service;

import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.dto.PaymentType;
import ba.paymentservice.exception.BadRequestException;
import ba.paymentservice.validator.*;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentValidationService {
    private final Map<PaymentType, PaymentValidator> validatorMap;

    public PaymentValidationService(List<PaymentValidator> validators) {
        this.validatorMap = validators.stream()
                .collect(Collectors.toMap(PaymentValidator::getSupportedPaymentType, v -> v, (v1,v2) -> v1, () -> new EnumMap<>(PaymentType.class)));
    }

    public void validate(PaymentCreationRequest request) {
        PaymentValidator validator = validatorMap.get(request.paymentType());
        if (validator == null) {
            throw new BadRequestException("Invalid payment type");
        }
        validator.validate(request);
    }
}
