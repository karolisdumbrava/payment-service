package ba.paymentservice.repository;

import ba.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Returns a projection with only the ID for non-canceled payments
    List<PaymentIdProjection> findByCanceledFalse();

    // This method return all payments that are canceled and have an equal amount to the provided one.
    List<PaymentIdProjection> findByCanceledAndAmount(Boolean canceled, BigDecimal amount);

}
