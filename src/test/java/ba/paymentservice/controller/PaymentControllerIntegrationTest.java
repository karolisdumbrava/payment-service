package ba.paymentservice.controller;

import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.dto.PaymentType;
import ba.paymentservice.model.Payment;
import ba.paymentservice.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
    }

    @Test
    void testCreatePayment() throws Exception {
        PaymentCreationRequest request = new PaymentCreationRequest();
        request.setPaymentType(PaymentType.TYPE1);
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setCurrency("EUR");
        request.setDebtorIban("DE89370400440532013000");
        request.setCreditorIban("DE75512108001245126199");
        request.setDetails("Payment for invoice 123");
        // For TYPE1, creditorBankBic is not required

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.amount", is(100.00)))
                .andExpect(jsonPath("$.currency", is("EUR")));
    }

    @Test
    void testCancelPaymentEndpoint() throws Exception {
        // First, create a payment directly using the repository.
        Payment payment = new Payment(
                PaymentType.TYPE1,
                BigDecimal.valueOf(100.00),
                "EUR",
                "DE89370400440532013000",
                "DE75512108001245126199",
                "Test Payment",
                null);
        // Set the creation time to now.
        payment.setCreatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        // Now, call the cancel endpoint.
        mockMvc.perform(post("/api/payments/{id}/cancel", payment.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canceled", is(true)))
                // For TYPE1, coefficient is 0.05. Using the current hour (e.g., if now is 10 AM, fee=0.5).
                .andExpect(jsonPath("$.cancellationFee", notNullValue()));
    }

    @Test
    void testGetNonCanceledPayments() throws Exception {
        // Create two payments using the repository.
        Payment payment1 = new Payment(PaymentType.TYPE1, BigDecimal.valueOf(100.00), "EUR",
                "DE89370400440532013000", "DE75512108001245126199", "Test 1", null);
        Payment payment2 = new Payment(PaymentType.TYPE1, BigDecimal.valueOf(200.00), "EUR",
                "DE89370400440532013000", "DE75512108001245126199", "Test 2", null);
        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        // GET /api/payments should return a JSON array of IDs.
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testGetPaymentCancellationResponse() throws Exception {
        // Create a payment and cancel it directly using the repository.
        Payment payment = new Payment(PaymentType.TYPE1, BigDecimal.valueOf(100.00), "EUR",
                "DE89370400440532013000", "DE75512108001245126199", "Test Payment", null);
        payment.setCreatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        // Cancel the payment using the service layer if needed, or simulate cancellation.
        // For demonstration, we'll update the payment directly.
        payment.cancelPayment(BigDecimal.valueOf(0.5));
        paymentRepository.save(payment);

        // GET /api/payments/{id} should return cancellation info (id and cancellationFee)
        mockMvc.perform(get("/api/payments/{id}", payment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(payment.getId().intValue())))
                .andExpect(jsonPath("$.cancellationFee", is(0.5)));
    }
}
