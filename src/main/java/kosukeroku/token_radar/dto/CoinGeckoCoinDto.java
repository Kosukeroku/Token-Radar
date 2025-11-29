package kosukeroku.token_radar.dto;

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
    private Integer market_cap_rank;

    private BigDecimal current_price;
    private Double price_change_24h;
    private Double price_change_percentage_24h;
    private BigDecimal market_cap;
    private BigDecimal total_volume;

    private Double price_change_percentage_1h_in_currency;
    private Double price_change_percentage_7d_in_currency;
    private Double price_change_percentage_30d_in_currency;

    private SparklineData sparkline_in_7d;

    private BigDecimal high_24h;
    private BigDecimal low_24h;


    private BigDecimal ath;
    private Double ath_change_percentage;
    private String ath_date;

    @Data
    public static class SparklineData {
        private List<Double> price;
    }
}