package ba.paymentservice.model;

import ba.paymentservice.dto.PaymentType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    private BigDecimal amount;
    private String currency;
    private String debtorIban;
    private String creditorIban;
    private String details;
    private String creditorBankBic;
    private LocalDateTime createdAt;
    private boolean canceled;
    private BigDecimal cancellationFee;

    // Constructor for JPA
    public Payment() {}

    public Payment(PaymentType paymentType, BigDecimal amount, String currency, String debtorIban, String creditorIban, String details, String creditorBankBic) {
        this.paymentType = paymentType;
        this.amount = amount;
        this.currency = currency;
        this.debtorIban = debtorIban;
        this.creditorIban = creditorIban;
        this.details = details;
        this.creditorBankBic = creditorBankBic;
        this.createdAt = LocalDateTime.now();
        this.canceled = false;
        this.cancellationFee = BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long l) {
        this.id = l;
    }

    // Getters and setters
    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDebtorIban() {
        return debtorIban;
    }

    public void setDebtorIban(String debtorIban) {
        this.debtorIban = debtorIban;
    }

    public String getCreditorIban() {
        return creditorIban;
    }

    public void setCreditorIban(String creditorIban) {
        this.creditorIban = creditorIban;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getCreditorBankBic() {
        return creditorBankBic;
    }

    public void setCreditorBankBic(String creditorBankBic) {
        this.creditorBankBic = creditorBankBic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void cancelPayment(BigDecimal fee) {
        this.canceled = true;
        this.cancellationFee = fee;
    }

    public BigDecimal getCancellationFee() {
        return cancellationFee;
    }

}
