package kosukeroku.token_radar.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TrackedCurrencyResponseDto {
    private Long id;
    private String coinId;
    private String coinName;
    private String coinSymbol;
    private String coinImageUrl;
    private Double threshold;
    private LocalDateTime addedAt;
    private BigDecimal currentPrice;
    private Double priceChange24h;
}