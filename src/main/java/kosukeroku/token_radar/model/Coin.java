package kosukeroku.token_radar.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coins")
@Data
public class Coin {
    @Id
    private String id;        // "bitcoin"
    private String symbol;    // "btc"
    private String name;      // "Bitcoin"
    private boolean active = true;
    private LocalDateTime lastUpdated;
    private String imageUrl;
    private Integer marketCapRank;

    private BigDecimal currentPrice;
    private Double priceChange24h;
    private Double priceChangePercentage24h;
    private BigDecimal marketCap;
    private BigDecimal totalVolume;
}