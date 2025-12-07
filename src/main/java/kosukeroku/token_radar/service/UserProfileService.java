package kosukeroku.token_radar.service;

import kosukeroku.token_radar.dto.UserProfileDto;
import kosukeroku.token_radar.dto.TrackedCurrencyResponseDto;
import kosukeroku.token_radar.exception.UserNotFoundException;
import kosukeroku.token_radar.mapper.UserMapper;
import kosukeroku.token_radar.model.User;
import kosukeroku.token_radar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final TrackedCurrencyService trackedCurrencyService;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // getting tracked currencies
        List<TrackedCurrencyResponseDto> trackedCurrencies =
                trackedCurrencyService.getUserTrackedCurrencies(userId);

        // getting count
        Long trackedCount = trackedCurrencyService.getUserTrackedCount(userId);

        // mapping user to dto
        UserProfileDto dto = userMapper.toDto(user);

        // adding trackedCurrencies
        dto.setTrackedCurrencies(trackedCurrencies);
        dto.setTrackedCount(trackedCount);

        return dto;
    }

}