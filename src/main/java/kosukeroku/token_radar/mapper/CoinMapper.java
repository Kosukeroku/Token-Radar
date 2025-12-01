package kosukeroku.token_radar.mapper;

import kosukeroku.token_radar.dto.CoinGeckoCoinDto;
import kosukeroku.token_radar.dto.CoinResponseDto;
import kosukeroku.token_radar.model.Coin;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CoinMapper {

    @Mapping(target = "lastUpdated", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "sparklineData", ignore = true) // mapping manually in service
    @Mapping(target = "athDate", ignore = true) // mapping manually in service
    Coin toEntity(CoinGeckoCoinDto dto);

    CoinResponseDto toResponseDto(Coin coin);
}