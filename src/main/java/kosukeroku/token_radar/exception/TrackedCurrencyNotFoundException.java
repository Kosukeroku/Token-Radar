package kosukeroku.token_radar.exception;

public class TrackedCurrencyNotFoundException extends RuntimeException {
    public TrackedCurrencyNotFoundException(Long userId, String coinId) {
        super("Tracked currency " + coinId + " not found for user: " + userId);
    }
}
