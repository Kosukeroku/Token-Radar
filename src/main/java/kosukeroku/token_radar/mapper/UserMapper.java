package kosukeroku.token_radar.mapper;

import kosukeroku.token_radar.dto.RegisterRequestDto;
import kosukeroku.token_radar.dto.UserProfileDto;
import kosukeroku.token_radar.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "trackedCurrencies", ignore = true)
    @Mapping(target = "trackedCount", ignore = true)
    UserProfileDto toDto(User user);

    User toEntity(RegisterRequestDto request);
}
