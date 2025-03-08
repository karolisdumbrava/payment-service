package ba.paymentservice.validator;

import ba.paymentservice.dto.Currency;
import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.exception.BadRequestException;
import org.springframework.util.StringUtils;

public class Type1PaymentValidator implements PaymentValidator {

    @Override
    public void validate(PaymentCreationRequest request) {
        // Currency validation
        Currency currency = Currency.valueOf(request.getCurrency().toUpperCase());
        if (currency != Currency.EUR) {
            throw new BadRequestException("Currency must be EUR for payment TYPE1");
        }

        // Details validation
        if (!StringUtils.hasText(request.getDetails())) {
            throw new BadRequestException("Details are required for payment type TYPE1");
        }
    }
}
