package kosukeroku.token_radar.exception;

public class TrackedCurrencyAlreadyExistsException extends RuntimeException {
    public TrackedCurrencyAlreadyExistsException(String coinId) {
        super("Currency already tracked: " + coinId);
    }
}
