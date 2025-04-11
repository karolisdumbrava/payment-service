package ba.paymentservice.controller;

import ba.paymentservice.dto.PaymentCancellationResponse;
import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.model.Payment;
import ba.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<?> createPayment(@Valid @RequestBody PaymentCreationRequest request) {
        try {
            var payment = paymentService.createAndSavePayment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<?> cancelPayment(@PathVariable Long paymentId) {
        try {
            var payment = paymentService.cancelPaymentById(paymentId);
            return ResponseEntity.ok(payment);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Long>> getNonCanceledPayments(@RequestParam(required = false) BigDecimal amount) {
        List<Long> paymentIds = paymentService.getNonCanceledPaymentIds(amount);
        return ResponseEntity.ok(paymentIds);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentCancellationResponse> getPaymentById(@PathVariable Long paymentId) {
        var response = paymentService.getPaymentCancellationResponse(paymentId);
        return ResponseEntity.ok(response);
    }
}
