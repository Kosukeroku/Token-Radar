package kosukeroku.token_radar.dto;

import lombok.Data;

import java.math.BigDecimal;
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

}