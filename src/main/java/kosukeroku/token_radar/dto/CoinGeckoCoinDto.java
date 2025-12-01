package kosukeroku.token_radar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class CoinGeckoCoinDto {
    private String id;
    private String symbol;
    private String name;
    private String image;

    @JsonProperty("market_cap_rank")
    private Integer marketCapRank;

    @JsonProperty("current_price")
    private BigDecimal currentPrice;

    @JsonProperty("price_change_24h")
    private Double priceChange24h;

    @JsonProperty("price_change_percentage_24h")
    private Double priceChangePercentage24h;

    @JsonProperty("market_cap")
    private BigDecimal marketCap;

    @JsonProperty("total_volume")
    private BigDecimal totalVolume;

    @JsonProperty("price_change_percentage_1h_in_currency")
    private Double priceChangePercentage1h;

    @JsonProperty("price_change_percentage_7d_in_currency")
    private Double priceChangePercentage7d;

    @JsonProperty("price_change_percentage_30d_in_currency")
    private Double priceChangePercentage30d;

    @JsonProperty("sparkline_in_7d")
    private SparklineData sparklineIn7d;

    @JsonProperty("high_24h")
    private BigDecimal high24h;

    @JsonProperty("low_24h")
    private BigDecimal low24h;

    private BigDecimal ath;

    @JsonProperty("ath_change_percentage")
    private Double athChangePercentage;

    @JsonProperty("ath_date")
    private String athDate;

    @Data
    public static class SparklineData {
        private List<Double> price;
    }
}