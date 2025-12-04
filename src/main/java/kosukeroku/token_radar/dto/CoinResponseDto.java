package kosukeroku.token_radar.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CoinResponseDto {
    private String id;
    private String symbol;
    private String name;
    private String imageUrl;
    private BigDecimal currentPrice;
    private LocalDateTime lastUpdated;
    private Double priceChange24h;
    private Double priceChangePercentage24h;
    private Integer marketCapRank;
    private BigDecimal marketCap;
    private BigDecimal totalVolume;
    private Double priceChangePercentage1h;
    private Double priceChangePercentage7d;
    private Double priceChangePercentage30d;
    private String sparklineData;
    private BigDecimal high24h;
    private BigDecimal low24h;
    private BigDecimal ath;
    private Double athChangePercentage;
    private LocalDateTime athDate;
    private BigDecimal atl;
    private Double atlChangePercentage;
    private LocalDateTime atlDate;
    private BigDecimal circulatingSupply;
}