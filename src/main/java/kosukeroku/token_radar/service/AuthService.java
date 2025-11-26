package kosukeroku.token_radar.service;

import kosukeroku.token_radar.dto.AuthResponseDto;
import kosukeroku.token_radar.dto.LoginRequestDto;
import kosukeroku.token_radar.dto.RegisterRequestDto;
import kosukeroku.token_radar.dto.UserResponseDto;
import kosukeroku.token_radar.exception.AuthenticationException;
import kosukeroku.token_radar.exception.EmailAlreadyExistsException;
import kosukeroku.token_radar.exception.UsernameAlreadyExistsException;
import kosukeroku.token_radar.mapper.UserMapper;
import kosukeroku.token_radar.model.User;
import kosukeroku.token_radar.repository.UserRepository;
import kosukeroku.token_radar.security.JwtUtil;
import kosukeroku.token_radar.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto request) {
        log.info("Login attempt for user: {}", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            String token = jwtUtil.generateToken(userDetails.getUsername());
            log.info("User logged in successfully: {}", request.getUsername());

            return new AuthResponseDto(token, "Bearer", userDetails.getId(), userDetails.getUsername(), userDetails.getEmail());
        } catch (BadCredentialsException e) {
            log.warn("Login failed for user {}: invalid credentials", request.getUsername());
            throw new AuthenticationException("Invalid credentials");
        }
    }

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        log.info("Attempting to register user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: username already exists: {}", request.getUsername());
            throw new UsernameAlreadyExistsException(request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email already exists: {}", request.getEmail());
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);

        log.info("User registered successfully: {}", savedUser.getUsername());

        String token = jwtUtil.generateToken(savedUser.getUsername());

        return new AuthResponseDto(token, "Bearer", savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
    }
}