package ba.paymentservice.validator;

import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.dto.PaymentType;
import ba.paymentservice.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class Type3PaymentValidator implements PaymentValidator {

    @Override
    public PaymentType getSupportedPaymentType() {
        return PaymentType.TYPE3;
    }

    @Override
    public void validate(PaymentCreationRequest request) {
        if (!StringUtils.hasText(request.creditorIban())) {
            throw new BadRequestException("Creditor IBAN is required for TYPE3 payment");
        }
        if (!StringUtils.hasText(request.creditorBankBic())) {
            throw new BadRequestException("Creditor bank BIC required for TYPE3 payment");
        }
    }
}
