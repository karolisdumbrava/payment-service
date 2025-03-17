package ba.paymentservice.model;

import ba.paymentservice.dto.Currency;
import ba.paymentservice.dto.PaymentType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
// For JPA
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Setter
    @Column(nullable = false)
    private BigDecimal amount;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Setter
    @Column(name = "debtor_iban", nullable = false, length = 34)
    private String debtorIban;

    @Setter
    @Column(name = "creditor_iban", nullable = false, length = 34)
    private String creditorIban;

    @Setter
    @Column
    private String details;

    @Setter
    @Column(name = "creditor_bank_bic", nullable = false, length = 11)
    private String creditorBankBic;

    @Setter
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Setter
    @Column(nullable = false)
    private boolean canceled;

    @Setter
    @Column(name = "cancellation_fee", nullable = false)
    private BigDecimal cancellationFee;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;
}
