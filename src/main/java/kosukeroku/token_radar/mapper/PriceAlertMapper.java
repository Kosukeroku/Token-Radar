package kosukeroku.token_radar.mapper;

import kosukeroku.token_radar.dto.PriceAlertResponseDto;
import kosukeroku.token_radar.model.PriceAlert;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PriceAlertMapper {

    @Mapping(source = "coin.id", target = "coinId")
    @Mapping(source = "coin.name", target = "coinName")
    @Mapping(source = "coin.symbol", target = "coinSymbol")
    @Mapping(source = "coin.imageUrl", target = "coinImageUrl")
    @Mapping(source = "coin.currentPrice", target = "currentPrice")
    @Mapping(source = "status", target = "isRead", qualifiedByName = "statusToIsRead")
    PriceAlertResponseDto toDto(PriceAlert alert);

    @Named("statusToIsRead")
    default Boolean statusToIsRead(kosukeroku.token_radar.model.enums.AlertStatus status) {
        return status == kosukeroku.token_radar.model.enums.AlertStatus.READ;
    }
}