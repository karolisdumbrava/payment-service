package ba.paymentservice.controller;

import ba.paymentservice.dto.Currency;
import ba.paymentservice.dto.PaymentCancellationResponse;
import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.dto.PaymentType;
import ba.paymentservice.exception.GlobalExceptionHandler;
import ba.paymentservice.exception.PaymentNotFoundException;
import ba.paymentservice.model.Payment;
import ba.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- createPayment tests ---
    @Test
    public void createPayment_ShouldReturnCreatedPayment() throws Exception {
        PaymentCreationRequest request = new PaymentCreationRequest(
                PaymentType.TYPE1,
                BigDecimal.valueOf(150.00),
                Currency.EUR,
                "DE12345678901234",
                "DE09876543210987",
                "Invoice #123",
                "BIC1234567",
                1L
        );

        Payment payment = Payment.builder()
                .id(1L)
                .paymentType(PaymentType.TYPE1)
                .amount(BigDecimal.valueOf(150.00))
                .currency(Currency.EUR)
                .debtorIban("DE12345678901234")
                .creditorIban("DE09876543210987")
                .details("Invoice #123")
                .creditorBankBic("BIC1234567")
                .createdAt(LocalDateTime.now())
                .canceled(false)
                .cancellationFee(BigDecimal.ZERO)
                .build();

        Mockito.when(paymentService.createAndSavePayment(any(PaymentCreationRequest.class)))
                .thenReturn(payment);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(payment.getId()))
                .andExpect(jsonPath("$.amount").value(payment.getAmount().doubleValue()))
                .andExpect(jsonPath("$.paymentType").value(payment.getPaymentType().toString()))
                .andExpect(jsonPath("$.currency").value(payment.getCurrency().toString()))
                .andExpect(jsonPath("$.debtorIban").value(payment.getDebtorIban()))
                .andExpect(jsonPath("$.creditorIban").value(payment.getCreditorIban()))
                .andExpect(jsonPath("$.details").value(payment.getDetails()))
                .andExpect(jsonPath("$.creditorBankBic").value(payment.getCreditorBankBic()))
                .andExpect(jsonPath("$.canceled").value(payment.isCanceled()))
                .andExpect(jsonPath("$.cancellationFee").value(payment.getCancellationFee().doubleValue()));
    }

    // --- cancelPayment tests ---
    @Test
    public void cancelPayment_ShouldReturnCanceledPayment() throws Exception {
        Long paymentId = 1L;
        Payment payment = Payment.builder()
                .id(paymentId)
                .paymentType(PaymentType.TYPE1)
                .amount(BigDecimal.valueOf(150.00))
                .currency(Currency.EUR)
                .debtorIban("DE12345678901234")
                .creditorIban("DE09876543210987")
                .details("Invoice #123")
                .creditorBankBic("BIC1234567")
                .createdAt(LocalDateTime.now())
                .canceled(true)
                .cancellationFee(BigDecimal.valueOf(3.00))
                .build();

        Mockito.when(paymentService.cancelPaymentById(paymentId))
                .thenReturn(payment);

        mockMvc.perform(post("/api/payments/{paymentId}/cancel", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId()))
                .andExpect(jsonPath("$.canceled").value(true))
                .andExpect(jsonPath("$.cancellationFee").value(payment.getCancellationFee().doubleValue()));
    }

    @Test
    public void cancelPayment_ShouldReturnBadRequest_WhenServiceThrowsException() throws Exception {
        Long paymentId = 1L;
        String errorMessage = "Payment already canceled";
        Mockito.when(paymentService.cancelPaymentById(paymentId))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(post("/api/payments/{paymentId}/cancel", paymentId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }

    // --- getNonCanceledPayments tests ---
    @Test
    public void getNonCanceledPayments_ShouldReturnListOfPaymentIds() throws Exception {
        List<Long> paymentIds = Arrays.asList(1L, 2L, 3L);
        BigDecimal amount = BigDecimal.valueOf(150.00);
        Mockito.when(paymentService.getNonCanceledPaymentIds(amount))
                .thenReturn(paymentIds);

        mockMvc.perform(get("/api/payments")
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1L))
                .andExpect(jsonPath("$[1]").value(2L))
                .andExpect(jsonPath("$[2]").value(3L));
    }

    @Test
    public void getNonCanceledPayments_ShouldReturnNotFoundError_WhenNoPaymentsFound() throws Exception {
        BigDecimal amount = BigDecimal.valueOf(150.00);
        String errorMessage = "No non-canceled payments found";
        Mockito.when(paymentService.getNonCanceledPaymentIds(amount))
                .thenThrow(new PaymentNotFoundException(errorMessage));

        mockMvc.perform(get("/api/payments")
                        .param("amount", amount.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(404))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    // --- getPaymentById tests ---
    @Test
    public void getPaymentById_ShouldReturnPaymentCancellationResponse() throws Exception {
        Long paymentId = 1L;
        PaymentCancellationResponse response = new PaymentCancellationResponse(paymentId, BigDecimal.valueOf(2.50));
        Mockito.when(paymentService.getPaymentCancellationResponse(paymentId))
                .thenReturn(response);

        mockMvc.perform(get("/api/payments/{paymentId}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.cancellationFee").value(response.cancellationFee().doubleValue()));
    }
}
