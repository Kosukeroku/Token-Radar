package kosukeroku.token_radar.service;

import kosukeroku.token_radar.dto.AuthResponseDto;
import kosukeroku.token_radar.dto.LoginRequestDto;
import kosukeroku.token_radar.dto.RegisterRequestDto;
import kosukeroku.token_radar.exception.AuthenticationException;
import kosukeroku.token_radar.exception.EmailAlreadyExistsException;
import kosukeroku.token_radar.exception.UsernameAlreadyExistsException;
import kosukeroku.token_radar.mapper.UserMapper;
import kosukeroku.token_radar.model.User;
import kosukeroku.token_radar.repository.UserRepository;
import kosukeroku.token_radar.security.JwtUtil;
import kosukeroku.token_radar.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@mail.com");
        registerRequest.setPassword("password");

        loginRequest = new LoginRequestDto();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@mail.com");
        user.setPassword("encodedPassword");
    }

    @Test
    void register_ShouldReturnAuthResponse_WhenRegistrationIsSuccessful() {
        // given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userMapper.toEntity(registerRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(jwtUtil.generateToken("testuser")).thenReturn("jwtToken");

        // when
        AuthResponseDto result = authService.register(registerRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwtToken");
        assertThat(result.getType()).isEqualTo("Bearer");
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@mail.com");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@mail.com");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(user);
        verify(jwtUtil).generateToken("testuser");
    }

    @Test
    void register_ShouldThrowUsernameAlreadyExistsException_WhenUsernameExists() {
        // given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessage("Username 'testuser' already exists.");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowEmailAlreadyExistsException_WhenEmailExists() {
        // given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@mail.com")).thenReturn(true);

        // then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email 'test@mail.com' already exists.");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@mail.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        // given
        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "testuser", "test@mail.com",
                "encodedPassword", List.of());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken("testuser")).thenReturn("jwtToken");

        // when
        AuthResponseDto result = authService.login(loginRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwtToken");
        assertThat(result.getType()).isEqualTo("Bearer");
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@mail.com");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken("testuser");
    }

    @Test
    void login_ShouldThrowAuthenticationException_WhenCredentialsAreInvalid() {
        // given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void login_ShouldThrowAuthenticationException_WhenUserNotFound() {
        // given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("User not found"));

        // then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any());
    }
}