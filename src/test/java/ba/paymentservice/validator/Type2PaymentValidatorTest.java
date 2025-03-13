package ba.paymentservice.validator;

import ba.paymentservice.dto.Currency;
import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.dto.PaymentType;
import ba.paymentservice.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class Type2PaymentValidatorTest {
    private final Type2PaymentValidator validator = new Type2PaymentValidator();

    private PaymentCreationRequest createPayment(Currency currency) {
        return new PaymentCreationRequest(
                PaymentType.TYPE2,
                BigDecimal.valueOf(150.00),
                currency,
                null,
                null,
                null,
                null,
                1L
        );
    }
    @Test
    public void whenCurrencyIsUSD_thenValidationSucceeds() {
        assertDoesNotThrow(() -> validator.validate(createPayment(Currency.USD)));
    }

    @Test
    public void whenCurrencyIsNotUSD_thenThrowBadRequestException() {
        // Act & Assert: Expect a BadRequestException with a specific error message
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> validator.validate(createPayment(Currency.EUR))
        );
        assertEquals("Currency must be USD for payment TYPE2", exception.getMessage());
    }
}
