package kosukeroku.token_radar.mapper;

import kosukeroku.token_radar.dto.RegisterRequestDto;
import kosukeroku.token_radar.dto.UserResponseDto;
import kosukeroku.token_radar.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toDto(User user);
    User toEntity(RegisterRequestDto request);
}
