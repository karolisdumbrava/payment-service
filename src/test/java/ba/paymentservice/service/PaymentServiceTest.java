package ba.paymentservice.service;

import ba.paymentservice.dto.PaymentCancellationResponse;
import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.dto.PaymentType;
import ba.paymentservice.model.Payment;
import ba.paymentservice.repository.PaymentIdProjection;
import ba.paymentservice.repository.PaymentRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        paymentService = new PaymentService(paymentRepository, validator);
    }

    @Test
    void testCreateAndSavePayment() {
        PaymentCreationRequest request = new PaymentCreationRequest();
        request.setPaymentType(PaymentType.TYPE1);
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setCurrency("EUR");
        request.setDebtorIban("DE89370400440532013000");
        request.setCreditorIban("DE75512108001245126199");
        request.setDetails("Payment for invoice 123");
        request.setCreditorBankBic(null); // Not required for TYPE1

        // When repository.save is called, return the payment with an ID set.
        Payment payment = new Payment(
                request.getPaymentType(),
                request.getAmount(),
                request.getCurrency(),
                request.getDebtorIban(),
                request.getCreditorIban(),
                request.getDetails(),
                request.getCreditorBankBic()
        );

        payment.setId(1L);

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        Payment savedPayment = paymentService.createAndSavePayment(request);

        assertNotNull(savedPayment);
        assertEquals(1L, savedPayment.getId());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testCancelPaymentByIdSuccess() {
        // Create a Payment that was created today with a specific hour.
        Payment payment = new Payment(PaymentType.TYPE1,
                BigDecimal.valueOf(100.00),
                "EUR",
                "DE89370400440532013000",
                "DE75512108001245126199",
                "Test Payment",
                null);
        // Force the creation time to a specific hour (e.g., 10 AM)
        LocalDateTime now = LocalDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0);
        payment.setCreatedAt(now);
        payment.setId(1L);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

        Payment canceledPayment = paymentService.cancelPaymentById(1L);

        // For TYPE1, coefficient is 0.05. At hour 10, fee should be 10 * 0.05 = 0.5
        BigDecimal expectedFee = BigDecimal.valueOf(10).multiply(BigDecimal.valueOf(0.05));
        assertTrue(canceledPayment.isCanceled());
        assertEquals(0, expectedFee.compareTo(canceledPayment.getCancellationFee()));

        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testGetNonCanceledPaymentIds() {
        // We'll simulate two projections for non-canceled payments.
        PaymentIdProjection proj1 = () -> 1L;
        PaymentIdProjection proj2 = () -> 2L;
        List<PaymentIdProjection> projections = Arrays.asList(proj1, proj2);

        // When no amount filter is present, use findByCanceledFalse
        when(paymentRepository.findByCanceledFalse()).thenReturn(projections);

        List<Long> result = paymentService.getNonCanceledPaymentIds(null);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));

        verify(paymentRepository, times(1)).findByCanceledFalse();
    }

    @Test
    void testGetPaymentCancellationResponse() {
        Payment payment = new Payment(PaymentType.TYPE1,
                BigDecimal.valueOf(100.00),
                "EUR",
                "DE89370400440532013000",
                "DE75512108001245126199",
                "Test Payment",
                null);
        payment.setId(1L);
        payment.cancelPayment(BigDecimal.valueOf(0.5));

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        PaymentCancellationResponse response = paymentService.getPaymentCancellationResponse(1L);
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(0, BigDecimal.valueOf(0.5).compareTo(response.getCancellationFee()));

        verify(paymentRepository, times(1)).findById(1L);
    }

    @Nested
    @DisplayName("TYPE1 Payment Tests")
    class Type1Tests {
        @Test
        void testType1FailsIfNotEur() {
            PaymentCreationRequest request = new PaymentCreationRequest();
            request.setPaymentType(PaymentType.TYPE1);
            request.setAmount(BigDecimal.valueOf(100.00));
            request.setCurrency("USD");
            request.setDebtorIban("DE89370400440532013000");
            request.setCreditorIban("DE75512108001245126199");
            request.setDetails("Payment for invoice 123");
            request.setCreditorBankBic(null); // Not required for TYPE1

            Exception exception = assertThrows(IllegalArgumentException.class, () -> paymentService.createAndSavePayment(request));

            assertEquals("Currency must be EUR for payment type TYPE1", exception.getMessage());
        }

        @Test
        void testType1FailsIfNoDetails() {
            PaymentCreationRequest request = new PaymentCreationRequest();
            request.setPaymentType(PaymentType.TYPE1);
            request.setAmount(BigDecimal.valueOf(100.00));
            request.setCurrency("EUR");
            request.setDebtorIban("DE89370400440532013000");
            request.setCreditorIban("DE75512108001245126199");
            request.setDetails(null);
            request.setCreditorBankBic(null); // Not required for TYPE1

            Exception exception = assertThrows(IllegalArgumentException.class, () -> paymentService.createAndSavePayment(request));

            assertEquals("Details are required for payment type TYPE1", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("TYPE2 Payment Tests")
    class Type2Tests {
        @Test
        void testType2FailsIfNotUsd() {
            PaymentCreationRequest request = new PaymentCreationRequest();
            request.setPaymentType(PaymentType.TYPE2);
            request.setAmount(BigDecimal.valueOf(100.00));
            request.setCurrency("EUR"); // INVALID: TYPE2 must be USD
            request.setDebtorIban("DE89370400440532013000");
            request.setCreditorIban("DE75512108001245126199");

            Exception exception = assertThrows(IllegalArgumentException.class, () -> paymentService.createAndSavePayment(request));

            assertEquals("Currency must be USD for payment type TYPE2", exception.getMessage());
        }

        @Test
        void testType2SucceedsWithoutDetails() {
            PaymentCreationRequest request = new PaymentCreationRequest();
            request.setPaymentType(PaymentType.TYPE2);
            request.setAmount(BigDecimal.valueOf(100.00));
            request.setCurrency("USD"); // VALID
            request.setDebtorIban("DE89370400440532013000");
            request.setCreditorIban("DE75512108001245126199");
            request.setDetails(null); // VALID: Details are optional

            when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
                Payment savedPayment = (Payment) i.getArguments()[0];
                savedPayment.setId(1L);
                return savedPayment;
            });

            Payment savedPayment = paymentService.createAndSavePayment(request);

            assertNotNull(savedPayment);
            assertEquals(1L, savedPayment.getId());
        }
    }

    @Nested
    @DisplayName("TYPE3 Payment Tests")
    class Type3Tests {
        @Test
        void testType3FailsIfBicMissing() {
            PaymentCreationRequest request = new PaymentCreationRequest();
            request.setPaymentType(PaymentType.TYPE3);
            request.setAmount(BigDecimal.valueOf(100.00));
            request.setCurrency("EUR"); // VALID: TYPE3 can be EUR or USD
            request.setDebtorIban("DE89370400440532013000");
            request.setCreditorIban("DE75512108001245126199");
            // Missing creditorBankBic for TYPE3

            Exception exception = assertThrows(IllegalArgumentException.class, () -> paymentService.createAndSavePayment(request));

            assertEquals("Creditor bank BIC is required for payment type TYPE3", exception.getMessage());

        }

        @Test
        void testType3SucceedsWithValidBic() {
            PaymentCreationRequest request = new PaymentCreationRequest();
            request.setPaymentType(PaymentType.TYPE3);
            request.setAmount(BigDecimal.valueOf(100.00));
            request.setCurrency("USD"); // VALID: TYPE3 can be EUR or USD
            request.setDebtorIban("DE89370400440532013000");
            request.setCreditorIban("DE75512108001245126199");
            request.setCreditorBankBic("BIC123456"); // VALID

            when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
                Payment savedPayment = (Payment) i.getArguments()[0];
                savedPayment.setId(1L);
                return savedPayment;
            });

            Payment savedPayment = paymentService.createAndSavePayment(request);

            assertNotNull(savedPayment);
            assertEquals(1L, savedPayment.getId());
        }
    }

    @Test
    void testGetNonCanceledPaymentIdsWithAmountFilter() {
        // Simulate two projections for payments with a specific amount.
        PaymentIdProjection proj1 = () -> 3L;
        PaymentIdProjection proj2 = () -> 4L;
        List<PaymentIdProjection> projections = Arrays.asList(proj1, proj2);

        // When amount filter is provided, ensure findByCanceledAndAmount is called.
        when(paymentRepository.findByCanceledAndAmount(false, BigDecimal.valueOf(150.00)))
                .thenReturn(projections);

        List<Long> result = paymentService.getNonCanceledPaymentIds(BigDecimal.valueOf(150.00));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(3L));
        assertTrue(result.contains(4L));

        verify(paymentRepository, times(1)).findByCanceledAndAmount(false, BigDecimal.valueOf(150.00));
    }

    @Test
    void testCancelPaymentFailsIfAlreadyCanceled() {
        Payment payment = new Payment(PaymentType.TYPE1,
                BigDecimal.valueOf(100.00),
                "EUR",
                "DE89370400440532013000",
                "DE75512108001245126199",
                "Test Payment",
                null);
        // Set a valid creation time (today)
        LocalDateTime now = LocalDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0);
        payment.setCreatedAt(now);
        payment.setId(1L);
        // Simulate already canceled
        payment.cancelPayment(BigDecimal.valueOf(0.5));

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> paymentService.cancelPaymentById(1L));
        assertEquals("Payment is already canceled", exception.getMessage());

        verify(paymentRepository, times(1)).findById(1L);
    }

    @Test
    void testCancelPaymentFailsIfNotCreatedToday() {
        Payment payment = new Payment(PaymentType.TYPE1,
                BigDecimal.valueOf(100.00),
                "EUR",
                "DE89370400440532013000",
                "DE75512108001245126199",
                "Test Payment",
                null);
        // Set creation time to yesterday.
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        payment.setCreatedAt(yesterday);
        payment.setId(1L);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> paymentService.cancelPaymentById(1L));
        assertEquals("Payment can only be canceled on the same day it was created", exception.getMessage());

        verify(paymentRepository, times(1)).findById(1L);
    }

}
