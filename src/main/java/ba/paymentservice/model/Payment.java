package ba.paymentservice.model;

import ba.paymentservice.dto.Currency;
import ba.paymentservice.dto.PaymentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
// For JPA
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(name = "debtor_iban", nullable = false, length = 34)
    private String debtorIban;

    @Column(name = "creditor_iban", nullable = false, length = 34)
    private String creditorIban;

    @Column
    private String details;

    @Column(name = "creditor_bank_bic", nullable = false, length = 11)
    private String creditorBankBic;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean canceled;

    @Column(name = "cancellation_fee", nullable = false)
    private BigDecimal cancellationFee;
}
