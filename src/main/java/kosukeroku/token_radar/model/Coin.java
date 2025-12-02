package kosukeroku.token_radar.model;

import jakarta.persistence.Column;
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
    private String id;
    private String symbol;
    private String name;
    private boolean active = true;
    private LocalDateTime lastUpdated;
    private String imageUrl;
    private Integer marketCapRank;

    // current prices
    private BigDecimal currentPrice;
    private Double priceChange24h;
    private Double priceChangePercentage24h;
    private BigDecimal marketCap;
    private BigDecimal totalVolume;

    // price changes
    private Double priceChangePercentage1h;
    private Double priceChangePercentage7d;
    private Double priceChangePercentage30d;

    @Column(columnDefinition = "TEXT")
    private String sparklineData; // sparkline in json

    private BigDecimal high24h;
    private BigDecimal low24h;

    // ATH data
    private BigDecimal ath;
    private Double athChangePercentage;
    private LocalDateTime athDate;

    //ATL data
    private BigDecimal atl;
    private Double atlChangePercentage;
    private LocalDateTime atlDate;

    // supply
    @Column(precision = 19, scale = 8)
    private BigDecimal circulatingSupply;
}