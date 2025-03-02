package ba.paymentservice.service;

import ba.paymentservice.dto.PaymentCancellationResponse;
import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.model.Payment;
import ba.paymentservice.repository.PaymentIdProjection;
import ba.paymentservice.repository.PaymentRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final Validator validator;

    public PaymentService(PaymentRepository paymentRepository, Validator validator) {
        this.paymentRepository = paymentRepository;
        this.validator = validator;
    }

    public Payment cancelPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (payment.isCanceled()) {
            throw new IllegalArgumentException("Payment is already canceled");
        }

        if (!payment.getCreatedAt().toLocalDate().isEqual(LocalDateTime.now().toLocalDate())) {
            throw new IllegalArgumentException("Payment can only be canceled on the same day it was created");
        }

        int hourOfCreation = payment.getCreatedAt().getHour();

        BigDecimal coefficient = switch (payment.getPaymentType()) {
            case TYPE1 -> BigDecimal.valueOf(0.05);
            case TYPE2 -> BigDecimal.valueOf(0.1);
            case TYPE3 -> BigDecimal.valueOf(0.15);
            // No need for default case as all enum values are covered
        };

        // Calculate cancellation fee h = hour of creation * k = coefficient
        BigDecimal fee = BigDecimal.valueOf(hourOfCreation).multiply(coefficient);

        payment.cancelPayment(fee);

        return paymentRepository.save(payment);
    }

    public Payment createPayment(PaymentCreationRequest request) {
        if (!("EUR".equalsIgnoreCase(request.getCurrency()) || "USD".equalsIgnoreCase(request.getCurrency()))) {
            throw new IllegalArgumentException("Currency must be either EUR or USD");
        }

        switch (request.getPaymentType()) {
            case TYPE1:
                if (!("EUR".equalsIgnoreCase(request.getCurrency()))) {
                    throw new IllegalArgumentException("Currency must be EUR for payment type TYPE1");
                }
                // Details are required for TYPE1 payments
                if (!StringUtils.hasText(request.getDetails())) {
                    throw new IllegalArgumentException("Details are required for payment type TYPE1");
                }
                break;
            case TYPE2:
                if (!("USD".equalsIgnoreCase(request.getCurrency()))) {
                    throw new IllegalArgumentException("Currency must be USD for payment type TYPE2");
                }
                // Details are optional for TYPE2 payments.
                break;
            case TYPE3:
                if (!StringUtils.hasText(request.getCreditorIban())) {
                    throw new IllegalArgumentException("Creditor IBAN is required for payment type TYPE3");
                }
                // Creditor bank BIC is required for TYPE3 payments
                if (!StringUtils.hasText(request.getCreditorBankBic())) {
                    throw new IllegalArgumentException("Creditor bank BIC is required for payment type TYPE3");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid payment type");
        }

        return new Payment(
                request.getPaymentType(),
                request.getAmount(),
                request.getCurrency(),
                request.getDebtorIban(),
                request.getCreditorIban(),
                request.getDetails(),
                request.getCreditorBankBic()
        );

    }

    public Payment createAndSavePayment(PaymentCreationRequest request) {
        Set<ConstraintViolation<PaymentCreationRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        Payment payment = createPayment(request);
        return paymentRepository.save(payment);
    }

    public List<Long> getNonCanceledPaymentIds(BigDecimal amount) {
        Optional<BigDecimal> optionalAmount = Optional.ofNullable(amount);
        List<PaymentIdProjection> projections;

        if (optionalAmount.isPresent()) {
            projections = paymentRepository.findByCanceledAndAmount(false, optionalAmount.get());
        } else {
            projections = paymentRepository.findByCanceledFalse();
        }

        return projections.stream()
                .map(PaymentIdProjection::getId)
                .collect(Collectors.toList());
    }

    public PaymentCancellationResponse getPaymentCancellationResponse(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        return new PaymentCancellationResponse(payment.getId(), payment.getCancellationFee());
    }

}
