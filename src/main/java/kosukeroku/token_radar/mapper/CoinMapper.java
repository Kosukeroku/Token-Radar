package kosukeroku.token_radar.mapper;

import kosukeroku.token_radar.dto.CoinGeckoCoinDto;
import kosukeroku.token_radar.dto.CoinResponseDto;
import kosukeroku.token_radar.model.Coin;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface CoinMapper {

    @Mapping(target = "imageUrl", source = "image")
    @Mapping(target = "marketCapRank", source = "market_cap_rank")
    @Mapping(target = "currentPrice", source = "current_price")
    @Mapping(target = "priceChange24h", source = "price_change_24h")
    @Mapping(target = "priceChangePercentage24h", source = "price_change_percentage_24h")
    @Mapping(target = "marketCap", source = "market_cap")
    @Mapping(target = "totalVolume", source = "total_volume")
    @Mapping(target = "lastUpdated", expression = "java(java.time.LocalDateTime.now())")
    Coin toEntity(CoinGeckoCoinDto dto);


    @Mapping(target = "imageUrl", source = "imageUrl")
    @Mapping(target = "currentPrice", source = "currentPrice")
    @Mapping(target = "priceChange24h", source = "priceChange24h")
    @Mapping(target = "marketCapRank", source = "marketCapRank")
    CoinResponseDto toResponseDto(Coin coin);
}