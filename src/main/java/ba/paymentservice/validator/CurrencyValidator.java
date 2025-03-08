package ba.paymentservice.validator;

import ba.paymentservice.dto.Currency;
import ba.paymentservice.exception.BadRequestException;

public class CurrencyValidator {
    public static Currency validateCurrency(String currency) {
        try {
            return Currency.valueOf(currency.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid currency: " + currency);
        }
    }
}
