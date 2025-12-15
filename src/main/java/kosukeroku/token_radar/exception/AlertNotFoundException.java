package kosukeroku.token_radar.exception;

public class AlertNotFoundException extends RuntimeException {
    public AlertNotFoundException(Long alertId) {
        super("Alert not found with id: " + alertId);
    }
}