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
    private LocalDateTime addedAt;
    private BigDecimal currentPrice;
    private Double priceChange24h;
    private Double priceChangePercentage24h;
    private Integer marketCapRank;

    private BigDecimal marketCap;
    private BigDecimal totalVolume;
}