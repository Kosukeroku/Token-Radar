package kosukeroku.token_radar.exception;

public class CoinNotFoundException extends RuntimeException {
    public CoinNotFoundException(String coinId) {
        super("Coin not found: " + coinId);
    }
}