package ba.paymentservice.service;

import ba.paymentservice.dto.Currency;
import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.dto.PaymentType;
import ba.paymentservice.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentValidationServiceStrategyTest {

    private PaymentValidationService paymentValidationService;

    @BeforeEach
    public void setUp() {
        paymentValidationService = new PaymentValidationService();
    }

    // ----- TYPE1 Tests -----
    @Test
    public void testType1ValidRequest() {
        PaymentCreationRequest request = new PaymentCreationRequest(
                PaymentType.TYPE1,
                BigDecimal.valueOf(100.00),
                Currency.EUR,
                "DE1234567890",
                "DE0987654321",
                "Some details",
                "BIC1234567",
                1L
        );
        assertDoesNotThrow(() -> paymentValidationService.validate(request));
    }

    @Test
    public void testType1InvalidCurrency() {
        PaymentCreationRequest request = new PaymentCreationRequest(
                PaymentType.TYPE1,
                BigDecimal.valueOf(100.00),
                Currency.USD, // Invalid: should be EUR for TYPE1
                "DE1234567890",
                "DE0987654321",
                "Some details",
                "BIC1234567",
                1L
        );
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> paymentValidationService.validate(request));
        assertEquals("Currency must be EUR for payment TYPE1", exception.getMessage());
    }

    @Test
    public void testType1MissingDetails() {
        PaymentCreationRequest request = new PaymentCreationRequest(
                PaymentType.TYPE1,
                BigDecimal.valueOf(100.00),
                Currency.EUR,
                "DE1234567890",
                "DE0987654321",
                "", // Missing details
                "BIC1234567",
                1L
        );
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> paymentValidationService.validate(request));
        assertEquals("Details are required for payment type TYPE1", exception.getMessage());
    }

    // ----- TYPE2 Tests -----
    @Test
    public void testType2ValidRequest() {
        PaymentCreationRequest request = new PaymentCreationRequest(
                PaymentType.TYPE2,
                BigDecimal.valueOf(200.00),
                Currency.USD,
                "DE1234567890",
                "DE0987654321",
                "Irrelevant details", // Not validated for TYPE2
                "BIC1234567",
                1L
        );
        assertDoesNotThrow(() -> paymentValidationService.validate(request));
    }

    @Test
    public void testType2InvalidCurrency() {
        PaymentCreationRequest request = new PaymentCreationRequest(
                PaymentType.TYPE2,
                BigDecimal.valueOf(200.00),
                Currency.EUR,  // Invalid: should be USD for TYPE2
                "DE1234567890",
                "DE0987654321",
                "Irrelevant details",
                "BIC1234567",
                1L
        );
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> paymentValidationService.validate(request));
        assertEquals("Currency must be USD for payment TYPE2", exception.getMessage());
    }

    // ----- TYPE3 Tests -----
    @Test
    public void testType3ValidRequest() {
        PaymentCreationRequest request = new PaymentCreationRequest(
                PaymentType.TYPE3,
                BigDecimal.valueOf(300.00),
                Currency.EUR,  // Currency is not validated for TYPE3
                "DE1234567890",
                "DE0987654321",
                "Some details",
                "BIC1234567",
                1L
        );
        assertDoesNotThrow(() -> paymentValidationService.validate(request));
    }

    @Test
    public void testType3MissingCreditorIban() {
        PaymentCreationRequest request = new PaymentCreationRequest(
                PaymentType.TYPE3,
                BigDecimal.valueOf(300.00),
                Currency.EUR,
                "DE1234567890",
                "", // Missing creditor IBAN
                "Some details",
                "BIC1234567",
                1L
        );
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> paymentValidationService.validate(request));
        assertEquals("Creditor IBAN is required for TYPE3 payment", exception.getMessage());
    }

    @Test
    public void testType3MissingCreditorBankBic() {
        PaymentCreationRequest request = new PaymentCreationRequest(
                PaymentType.TYPE3,
                BigDecimal.valueOf(300.00),
                Currency.EUR,
                "DE1234567890",
                "DE0987654321",
                "Some details",
                "", // Missing creditor bank BIC
                1L
        );
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> paymentValidationService.validate(request));
        assertEquals("Creditor bank BIC required for TYPE3 payment", exception.getMessage());
    }
}
