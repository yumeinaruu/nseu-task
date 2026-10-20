package com.yumeinaruu.nseutask.security;

import com.yumeinaruu.nseutask.config.JwtProperties;
import com.yumeinaruu.nseutask.entity.User;
import com.yumeinaruu.nseutask.entity.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceImplTest {

    private static final String SECRET = "test-secret-0123456789-0123456789-0123456789-0123456789-abcdef";
    private static final String OTHER_SECRET = "other-secret-0123456789-0123456789-0123456789-0123456789-abcd";
    private static final String ISSUER = "nseutask";
    private static final Duration EXPIRATION = Duration.ofHours(1);
    private static final Instant NOW = Instant.parse("2026-10-19T10:00:00Z");
    private static final String USERNAME = "alice";

    @Test
    void givenGeneratedToken_whenFindingUsername_thenReturnsSubject() {
        JwtServiceImpl jwtService = jwtService(SECRET, ISSUER, NOW);

        String token = jwtService.generateToken(user());

        assertThat(jwtService.findUsernameByToken(token)).contains(USERNAME);
    }

    @Test
    void givenExpiredToken_whenFindingUsername_thenReturnsEmpty() {
        String token = jwtService(SECRET, ISSUER, NOW).generateToken(user());
        JwtServiceImpl laterService = jwtService(SECRET, ISSUER, NOW.plus(EXPIRATION).plusSeconds(1));

        assertThat(laterService.findUsernameByToken(token)).isEmpty();
    }

    @Test
    void givenTokenSignedWithOtherKey_whenFindingUsername_thenReturnsEmpty() {
        String token = jwtService(OTHER_SECRET, ISSUER, NOW).generateToken(user());

        assertThat(jwtService(SECRET, ISSUER, NOW).findUsernameByToken(token)).isEmpty();
    }

    @Test
    void givenTokenFromOtherIssuer_whenFindingUsername_thenReturnsEmpty() {
        String token = jwtService(SECRET, "another-issuer", NOW).generateToken(user());

        assertThat(jwtService(SECRET, ISSUER, NOW).findUsernameByToken(token)).isEmpty();
    }

    @Test
    void givenGarbageToken_whenFindingUsername_thenReturnsEmpty() {
        assertThat(jwtService(SECRET, ISSUER, NOW).findUsernameByToken("not-a-jwt")).isEmpty();
    }

    private JwtServiceImpl jwtService(String secret, String issuer, Instant now) {
        return new JwtServiceImpl(
                new JwtProperties(secret, EXPIRATION, issuer),
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    private User user() {
        User user = new User();
        user.setUsername(USERNAME);
        user.setRole(Role.USER);
        return user;
    }

}
