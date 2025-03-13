package ba.paymentservice.validator;

import static org.junit.jupiter.api.Assertions.*;

import ba.paymentservice.dto.PaymentType;
import org.junit.jupiter.api.Test;
import ba.paymentservice.dto.Currency;
import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.exception.BadRequestException;

import java.math.BigDecimal;

public class Type1PaymentValidatorTest {

    private final Type1PaymentValidator validator = new Type1PaymentValidator();

    private PaymentCreationRequest createPayment(Currency currency, String details) {
        return new PaymentCreationRequest (
                PaymentType.TYPE1,
                BigDecimal.valueOf(100.00),
                currency,
                "DE1234567890",
                "DE0987654321",
                details,
                "BIC1234567",
                1L
        );
    }

    @Test
    public void whenCurrencyIsNotEUR_thenThrowException() {
        assertThrows(BadRequestException.class, () -> validator.validate(createPayment(Currency.USD, "Some details")));
    }

    @Test
    public void whenDetailsAreMissing_thenThrowException() {
        assertThrows(BadRequestException.class, () -> validator.validate(createPayment(Currency.EUR, "")));
    }

    @Test
    public void whenValidRequest_thenNoException() {
        assertDoesNotThrow(() -> validator.validate(createPayment(Currency.EUR, "Some details")));
    }

}
