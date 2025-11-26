package kosukeroku.token_radar.controller;

import jakarta.validation.Valid;
import kosukeroku.token_radar.dto.AuthResponseDto;
import kosukeroku.token_radar.dto.LoginRequestDto;
import kosukeroku.token_radar.dto.RegisterRequestDto;
import kosukeroku.token_radar.model.User;
import kosukeroku.token_radar.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        return authService.register(request);
    }
}
