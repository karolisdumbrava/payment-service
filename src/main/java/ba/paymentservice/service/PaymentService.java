package ba.paymentservice.service;

import ba.paymentservice.dto.PaymentCancellationResponse;
import ba.paymentservice.dto.PaymentCreationRequest;
import ba.paymentservice.exception.BadRequestException;
import ba.paymentservice.exception.PaymentAlreadyCanceledException;
import ba.paymentservice.exception.PaymentNotFoundException;
import ba.paymentservice.model.Payment;
import ba.paymentservice.repository.PaymentIdProjection;
import ba.paymentservice.repository.PaymentRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

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
    private final PaymentValidationService paymentValidationService;

    public PaymentService(PaymentRepository paymentRepository, Validator validator, PaymentValidationService paymentValidationService) {
        this.paymentRepository = paymentRepository;
        this.validator = validator;
        this.paymentValidationService = paymentValidationService;
    }

    public Payment cancelPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for ID: " + paymentId));

        if (payment.isCanceled()) {
            throw new PaymentAlreadyCanceledException("Payment is already canceled");
        }

        if (!payment.getCreatedAt().toLocalDate().isEqual(LocalDateTime.now().toLocalDate())) {
            throw new BadRequestException("Payment can only be canceled on the same day it was created");
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

        payment.setCancellationFee(fee);
        payment.setCanceled(true);

        return paymentRepository.save(payment);
    }

    public Payment createPayment(PaymentCreationRequest request) {
        // Validate payment.
        paymentValidationService.validate(request);

        return new Payment(
                null,
                null,
                request.paymentType(),
                request.amount(),
                request.currency(),
                request.debtorIban(),
                request.creditorIban(),
                request.details(),
                request.creditorBankBic(),
                LocalDateTime.now(),
                false,
                BigDecimal.ZERO
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

        if (projections.isEmpty()) {
            throw new PaymentNotFoundException("No non-canceled payments found");
        }

        return projections.stream()
                .map(PaymentIdProjection::getId)
                .collect(Collectors.toList());
    }

    public PaymentCancellationResponse getPaymentCancellationResponse(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        return new PaymentCancellationResponse(payment.getId(), payment.getCancellationFee());
    }

}
