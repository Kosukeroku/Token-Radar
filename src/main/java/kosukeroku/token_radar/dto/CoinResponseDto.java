package kosukeroku.token_radar.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoinResponseDto {
    private String id;
    private String symbol;
    private String name;
    private String imageUrl;
    private BigDecimal currentPrice;
    private Double priceChange24h;
    private Double priceChangePercentage24h;
    private Integer marketCapRank;
    private BigDecimal marketCap;
    private BigDecimal totalVolume;
}
