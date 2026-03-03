package me.rentsignal.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.user.dto.TokenDto;
import me.rentsignal.user.entity.Role;
import me.rentsignal.user.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    public static final long EXPIRE_ACCESS = 1000 * 60 * 10; // 10분
    public static final long EXPIRE_REFRESH = 1000 * 60 * 60 * 24 * 7; // 1주
    private final RefreshTokenRepository refreshTokenRepository;
    private SecretKey secretKey;

    @Value("${JWT_SECRET}")
    private String secret;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /** 토큰 (사용자 id + role 포함) 생성 */
    public TokenDto createTokens(Long id, Role role) {
        Date now = new Date();

        String accessToken = Jwts.builder().setSubject(String.valueOf(id)).claim("role", role.name())
                .setIssuedAt(now).setExpiration(new Date(now.getTime() + EXPIRE_ACCESS)).signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        String refreshToken = Jwts.builder().setSubject(String.valueOf(id)).claim("role", role.name())
                .setIssuedAt(now).setExpiration(new Date(now.getTime() + EXPIRE_REFRESH)).signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        return new TokenDto(accessToken, refreshToken);
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(secretKey).build().
                    parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            throw new CredentialsExpiredException("만료된 토큰입니다.");
        } catch (Exception e) {
            throw new BadCredentialsException("유효하지 않은 토큰입니다.");
        }
    }

    /** subject로 설정해놓은 userId 가져오기 */
    public Long getUserIdFromToken(String token) {
        String subject = parseClaims(token).getSubject();
        if (subject == null)
            throw new BadCredentialsException("토큰에 subject가 없습니다.");

        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException e) {
            throw new BadCredentialsException("토큰의 subject 값이 숫자가 아닙니다.");
        }
    }

    /** claim으로 설정해놓은 role 가져오기 */
    public Role getRoleFromToken(String token) {
        Object roleObj = parseClaims(token).get("role");
        if (roleObj == null)
            throw new BadCredentialsException("토큰에 role 정보가 없습니다.");

        String role = roleObj.toString();
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException e) { // role이 ROLE_... 형태가 아닌 경우
            throw new BadCredentialsException("토큰의 role 값이 올바르지 않습니다.");
        }
    }

}
