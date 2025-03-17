package ba.paymentservice.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Setter
    @Column(nullable = false, unique = true)
    private String username;

    // One user can have many payments.
    // Deleting user will delete associated payments
    // Removing a payment will unassign it and delete it (orphanRemoval)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Setter
    private List<Payment> payments = new ArrayList<>();

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setUser(this);
    }

    public void removePayment(Payment payment) {
        payments.remove(payment);
        payment.setUser(null);
    }
}
