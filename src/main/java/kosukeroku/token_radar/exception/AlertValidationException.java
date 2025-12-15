package kosukeroku.token_radar.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class AlertValidationException extends RuntimeException {
    public AlertValidationException(String message) {
        super(message);
    }
}