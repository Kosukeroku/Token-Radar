package kosukeroku.token_radar.exception;

public class CoinGeckoApiException extends RuntimeException {
    public CoinGeckoApiException(String message) {
        super("CoinGecko API error: " + message);
    }
}
