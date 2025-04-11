package ba.paymentservice.controller;

import ba.paymentservice.dto.UserCreationRequest;
import ba.paymentservice.model.User;
import ba.paymentservice.service.PaymentService;
import ba.paymentservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserService userService;
    private final PaymentService paymentService;

    public UserController(UserService userService, PaymentService paymentService) {
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserCreationRequest request) {
        var createdUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{userId}/payments")
    public ResponseEntity<List<Long>> getPaymentIdsByUser(@PathVariable Long userId) {
        List<Long> paymentIds = paymentService.getPaymentIdsByUser(userId);
        return ResponseEntity.ok(paymentIds);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        // 204 No content
        return ResponseEntity.noContent().build();
    }
}
