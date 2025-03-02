package ba.paymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class PaymentAlreadyCanceledException extends RuntimeException {

    public PaymentAlreadyCanceledException(String message) {
        super(message);
    }
}
