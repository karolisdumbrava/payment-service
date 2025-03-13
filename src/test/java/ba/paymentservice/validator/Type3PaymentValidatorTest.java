package ba.paymentservice.validator;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import ba.paymentservice.dto.Currency;
import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.dto.PaymentType;
import ba.paymentservice.exception.BadRequestException;

public class Type3PaymentValidatorTest {

    private final Type3PaymentValidator validator = new Type3PaymentValidator();

    private PaymentCreationRequest createPayment(String creditorIban, String creditorBankBic) {
        return new PaymentCreationRequest(
                PaymentType.TYPE3,
                BigDecimal.valueOf(150.00),
                Currency.USD,
                null,
                creditorIban,
                "",
                creditorBankBic,
                1L
        );
    }

    @Test
    public void whenCreditorIbanAndBankBicProvided_thenValidationSucceeds() {
        PaymentCreationRequest request = createPayment("DE1234567890", "BANKBIC");
        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    public void whenCreditorIbanIsMissing_thenThrowBadRequestException() {
        PaymentCreationRequest request = createPayment("", "BANKBIC");
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> validator.validate(request)
        );
        assertEquals("Creditor IBAN is required for TYPE3 payment", exception.getMessage());
    }

    @Test
    public void whenCreditorBankBicIsMissing_thenThrowBadRequestException() {
        PaymentCreationRequest request = createPayment("DE1234567890", "");
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> validator.validate(request)
        );
        assertEquals("Creditor bank BIC required for TYPE3 payment", exception.getMessage());
    }

    @Test
    public void whenBothCreditorIbanAndBankBicAreMissing_thenThrowBadRequestException() {
        PaymentCreationRequest request = createPayment(null, null);
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> validator.validate(request)
        );
        assertEquals("Creditor IBAN is required for TYPE3 payment", exception.getMessage());
    }
}
