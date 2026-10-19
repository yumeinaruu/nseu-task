package com.yumeinaruu.nseutask.security;

import com.yumeinaruu.nseutask.config.JwtProperties;
import com.yumeinaruu.nseutask.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    private static final String ROLE_CLAIM = "role";

    private final JwtProperties jwtProperties;
    private final Clock clock;
    private final SecretKey signingKey;

    public JwtServiceImpl(JwtProperties jwtProperties, Clock clock) {
        this.jwtProperties = jwtProperties;
        this.clock = clock;
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateToken(User user) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(jwtProperties.expiration());
        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(user.getUsername())
                .claim(ROLE_CLAIM, user.getRole().name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public Optional<String> findUsernameByToken(String token) {
        try {
            String username = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(jwtProperties.issuer())
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Optional.ofNullable(username);
        } catch (JwtException | IllegalArgumentException exception) {
            log.warn("Failed token validation: type={}, message={}",
                    exception.getClass().getSimpleName(), exception.getMessage());
            return Optional.empty();
        }
    }

}
