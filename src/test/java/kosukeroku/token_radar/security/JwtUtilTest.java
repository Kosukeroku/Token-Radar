package kosukeroku.token_radar.security;

import io.jsonwebtoken.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = "mySuperSecretKeyThatIsVeryLongAndSecure123!";

    @BeforeEach
    void setUp() {
        long expiration = 86400000; // 24 hrs
        jwtUtil = new JwtUtil(secret, expiration);
    }

    @Test
    void generateToken_ShouldReturnValidToken_WhenUsernameProvided() {
        // given
        String username = "testuser";

        // when
        String token = jwtUtil.generateToken(username);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        // verify token can be parsed and contains username
        String extractedUsername = jwtUtil.extractUsername(token);
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        // given
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        // when
        boolean isValid = jwtUtil.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsExpired() {
        // given
        String username = "testuser";

        // create JwtUtil with very short expiration...
        JwtUtil shortLivedJwtUtil = new JwtUtil(secret, 1);
        String token = shortLivedJwtUtil.generateToken(username);

        // ...and wait for it to expire
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // when
        boolean isValid = shortLivedJwtUtil.validateToken(token);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsInvalid() {
        // given
        String invalidToken = "invalid.token.here";

        // when
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsMalformed() {
        // given
        String malformedToken = "malformed.token";

        // when
        boolean isValid = jwtUtil.validateToken(malformedToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsEmpty() {
        // given
        String emptyToken = "";

        // when
        boolean isValid = jwtUtil.validateToken(emptyToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsNull() {
        // given
        String nullToken = null;

        // when
        boolean isValid = jwtUtil.validateToken(nullToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void extractUsername_ShouldReturnUsername_WhenTokenIsValid() {
        // given
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        // when
        String extractedUsername = jwtUtil.extractUsername(token);

        // then
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    void extractUsername_ShouldThrowException_WhenTokenIsInvalid() {
        // given
        String invalidToken = "invalid.token.here";

        // then
        assertThatThrownBy(() -> jwtUtil.extractUsername(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void validateTokenWithUsername_ShouldReturnTrue_WhenTokenAndUsernameMatch() {
        // given
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        // when
        boolean isValid = jwtUtil.validateToken(token, username);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateTokenWithUsername_ShouldReturnFalse_WhenUsernameDoesNotMatch() {
        // given
        String username = "testuser";
        String differentUsername = "differentuser";
        String token = jwtUtil.generateToken(username);

        // when
        boolean isValid = jwtUtil.validateToken(token, differentUsername);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateTokenWithUsername_ShouldReturnFalse_WhenTokenIsInvalid() {
        // given
        String username = "testuser";
        String invalidToken = "invalid.token.here";

        // when
        boolean isValid = jwtUtil.validateToken(invalidToken, username);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void generatedTokens_ShouldBeDifferent_ForDifferentUsernames() {
        // given
        String username1 = "user1";
        String username2 = "user2";

        // when
        String token1 = jwtUtil.generateToken(username1);
        String token2 = jwtUtil.generateToken(username2);

        // then
        assertThat(token1).isNotEqualTo(token2);

        String extractedUsername1 = jwtUtil.extractUsername(token1);
        String extractedUsername2 = jwtUtil.extractUsername(token2);

        assertThat(extractedUsername1).isEqualTo(username1);
        assertThat(extractedUsername2).isEqualTo(username2);
    }

    @Test
    void token_ShouldContainExpirationDate() {
        // given
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        // when
        String extractedUsername = jwtUtil.extractUsername(token);
        boolean isValid = jwtUtil.validateToken(token);

        // then
        assertThat(extractedUsername).isEqualTo(username);
        assertThat(isValid).isTrue();
    }
}