package kosukeroku.token_radar.controller;

import kosukeroku.token_radar.dto.UserProfileDto;
import kosukeroku.token_radar.security.UserDetailsImpl;
import kosukeroku.token_radar.service.UserProfileService;
import kosukeroku.token_radar.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    @GetMapping
    public ResponseEntity<UserProfileDto> getProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserProfileDto profile = profileService.getUserProfile(userDetails.getId());
        return ResponseEntity.ok(profile);
    }
}
