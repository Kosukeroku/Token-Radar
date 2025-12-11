package kosukeroku.token_radar.service;

import kosukeroku.token_radar.dto.TrackedCurrencyResponseDto;
import kosukeroku.token_radar.dto.UserProfileDto;
import kosukeroku.token_radar.exception.UserNotFoundException;
import kosukeroku.token_radar.mapper.UserMapper;
import kosukeroku.token_radar.model.User;
import kosukeroku.token_radar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrackedCurrencyService trackedCurrencyService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserProfileService userProfileService;

    private User testUser;
    private UserProfileDto testProfileDto;
    private TrackedCurrencyResponseDto testTrackedCurrencyDto;

    @BeforeEach
    void setUp() {
        // setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setCreatedAt(LocalDateTime.now());

        // setup test profile DTO
        testProfileDto = new UserProfileDto(
                1L, "testuser", "test@example.com",
                LocalDateTime.now(), List.of(), 0L
        );

        // setup test tracked currency DTO
        testTrackedCurrencyDto = new TrackedCurrencyResponseDto();
        testTrackedCurrencyDto.setCoinId("bitcoin");
        testTrackedCurrencyDto.setCoinName("Bitcoin");
    }

    @Test
    void getUserProfile_Success() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(trackedCurrencyService.getUserTrackedCurrencies(1L)).thenReturn(List.of(testTrackedCurrencyDto));
        when(trackedCurrencyService.getUserTrackedCount(1L)).thenReturn(1L);
        when(userMapper.toDto(testUser)).thenReturn(testProfileDto);

        // when
        UserProfileDto result = userProfileService.getUserProfile(1L);

        // then
        assertThat(result)
                .isNotNull()
                .satisfies(profile -> {
                    assertThat(profile.getUsername()).isEqualTo("testuser");
                    assertThat(profile.getEmail()).isEqualTo("test@example.com");
                    assertThat(profile.getTrackedCurrencies())
                            .hasSize(1)
                            .first()
                            .satisfies(dto -> {
                                assertThat(dto.getCoinId()).isEqualTo("bitcoin");
                                assertThat(dto.getCoinName()).isEqualTo("Bitcoin");
                            });
                    assertThat(profile.getTrackedCount()).isEqualTo(1L);
                });

        verify(trackedCurrencyService).getUserTrackedCurrencies(1L);
        verify(trackedCurrencyService).getUserTrackedCount(1L);
    }

    @Test
    void getUserProfile_UserNotFound() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> userProfileService.getUserProfile(1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(trackedCurrencyService, never()).getUserTrackedCurrencies(anyLong());
        verify(trackedCurrencyService, never()).getUserTrackedCount(anyLong());
    }

    @Test
    void getUserProfile_EmptyTrackedCurrencies() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(trackedCurrencyService.getUserTrackedCurrencies(1L)).thenReturn(List.of());
        when(trackedCurrencyService.getUserTrackedCount(1L)).thenReturn(0L);
        when(userMapper.toDto(testUser)).thenReturn(testProfileDto);

        // when
        UserProfileDto result = userProfileService.getUserProfile(1L);

        // then
        assertThat(result)
                .isNotNull()
                .satisfies(profile -> {
                    assertThat(profile.getTrackedCurrencies()).isEmpty();
                    assertThat(profile.getTrackedCount()).isZero();
                });
    }

    @Test
    void getUserProfile_MultipleTrackedCurrencies() {
        // given
        TrackedCurrencyResponseDto secondCurrency = new TrackedCurrencyResponseDto();
        secondCurrency.setCoinId("ethereum");
        secondCurrency.setCoinName("Ethereum");

        List<TrackedCurrencyResponseDto> currencies = List.of(testTrackedCurrencyDto, secondCurrency);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(trackedCurrencyService.getUserTrackedCurrencies(1L)).thenReturn(currencies);
        when(trackedCurrencyService.getUserTrackedCount(1L)).thenReturn(2L);
        when(userMapper.toDto(testUser)).thenReturn(testProfileDto);

        // when
        UserProfileDto result = userProfileService.getUserProfile(1L);

        // then
        assertThat(result.getTrackedCurrencies())
                .hasSize(2)
                .extracting(TrackedCurrencyResponseDto::getCoinId)
                .containsExactly("bitcoin", "ethereum");

        assertThat(result.getTrackedCount()).isEqualTo(2L);
    }
}