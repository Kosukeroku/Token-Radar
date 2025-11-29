package kosukeroku.token_radar.mapper;

import kosukeroku.token_radar.dto.CoinGeckoCoinDto;
import kosukeroku.token_radar.dto.CoinResponseDto;
import kosukeroku.token_radar.model.Coin;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CoinMapper {

    @Mapping(target = "imageUrl", source = "image")
    @Mapping(target = "marketCapRank", source = "market_cap_rank")
    @Mapping(target = "currentPrice", source = "current_price")
    @Mapping(target = "priceChange24h", source = "price_change_24h")
    @Mapping(target = "priceChangePercentage24h", source = "price_change_percentage_24h")
    @Mapping(target = "marketCap", source = "market_cap")
    @Mapping(target = "totalVolume", source = "total_volume")
    @Mapping(target = "priceChangePercentage1h", source = "price_change_percentage_1h_in_currency")
    @Mapping(target = "priceChangePercentage7d", source = "price_change_percentage_7d_in_currency")
    @Mapping(target = "priceChangePercentage30d", source = "price_change_percentage_30d_in_currency")
    @Mapping(target = "high24h", source = "high_24h")
    @Mapping(target = "low24h", source = "low_24h")
    @Mapping(target = "ath", source = "ath")
    @Mapping(target = "athChangePercentage", source = "ath_change_percentage")
    @Mapping(target = "lastUpdated", expression = "java(java.time.LocalDateTime.now())")

    @Mapping(target = "sparklineData", ignore = true)  // can't map with MapStruct (why?), will do manually
    @Mapping(target = "athDate", ignore = true)        // can't map with MapStruct (why?), will do manually
    Coin toEntity(CoinGeckoCoinDto dto);

    @Mapping(target = "imageUrl", source = "imageUrl")
    @Mapping(target = "currentPrice", source = "currentPrice")
    @Mapping(target = "priceChange24h", source = "priceChange24h")
    @Mapping(target = "marketCapRank", source = "marketCapRank")
    @Mapping(target = "marketCap", source = "marketCap")
    @Mapping(target = "totalVolume", source = "totalVolume")
    @Mapping(target = "priceChangePercentage1h", source = "priceChangePercentage1h")
    @Mapping(target = "priceChangePercentage7d", source = "priceChangePercentage7d")
    @Mapping(target = "priceChangePercentage30d", source = "priceChangePercentage30d")
    @Mapping(target = "sparklineData", source = "sparklineData")
    @Mapping(target = "high24h", source = "high24h")
    @Mapping(target = "low24h", source = "low24h")
    @Mapping(target = "ath", source = "ath")
    @Mapping(target = "athChangePercentage", source = "athChangePercentage")
    @Mapping(target = "athDate", source = "athDate")
    CoinResponseDto toResponseDto(Coin coin);
}