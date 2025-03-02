package ba.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PaymentCreationRequest {

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency; // "EUR" or "USD"

    @NotBlank(message = "Debtor IBAN is required")
    private String debtorIban;

    @NotBlank(message = "Creditor IBAN is required")
    private String creditorIban;

    // Type 1 and Type 2 payments require details
    private String details;

    // Type 3 payments require creditor bank BIC
    private String creditorBankBic;

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

    public String getCreditorBankBic() {
        return creditorBankBic;
    }

    public void setCreditorBankBic(String creditorBankBic) {
        this.creditorBankBic = creditorBankBic;
    }

    public void setDetails(String s) {
        this.details = s;
    }
}
