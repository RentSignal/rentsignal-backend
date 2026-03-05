package me.rentsignal.user.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.global.security.CookieUtil;
import me.rentsignal.global.security.JwtProvider;
import me.rentsignal.user.dto.TokenDto;
import me.rentsignal.user.entity.RefreshToken;
import me.rentsignal.user.entity.User;
import me.rentsignal.user.repository.RefreshTokenRepository;
import me.rentsignal.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {

    @Value("${COOKIE_SECURE}")
    private boolean COOKIE_SECURE;

    @Value("${COOKIE_SAMESITE}")
    private String COOKIE_SAMESITE;

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final CookieUtil cookieUtil;

    /** access / refresh 토큰 생성 및 refresh token 저장 */
    @Transactional
    public TokenDto createAndSaveToken(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BaseException(ErrorCode.USER_NOT_FOUND, "해당 id의 사용자를 찾을 수 없습니다."));

        TokenDto tokens = jwtProvider.createTokens(userId, user.getRole());

        // refresh token rotate - 기존 값 있는 경우 갱신, 없으면 새로 생성
        refreshTokenRepository.findByUser_Id(userId)
                .ifPresentOrElse(
                        rt -> rt.updateRefreshToken(tokens.getRefreshToken()),
                        () -> refreshTokenRepository.save(RefreshToken.builder()
                                    .user(user).refreshToken(tokens.getRefreshToken()).build())
                );
        return tokens;
    }

    /** access token 만료 시 토큰 재발급 */
    @Transactional
    public void reissue(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank())
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "refresh token 값이 누락되었습니다.");

        // 1. refreshToken 검증
        Long userIdFromToken;
        try {
            userIdFromToken = jwtProvider.getUserIdFromToken(refreshToken);
        } catch (CredentialsExpiredException e) {
            throw new BaseException(ErrorCode.EXPIRED_TOKEN);
        } catch (BadCredentialsException e) {
            throw new BaseException(ErrorCode.INVALID_TOKEN);
        }

        // 2. DB에 저장된 refresh token 조회
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUser_Id(userIdFromToken)
                .orElseThrow(() -> new BaseException(ErrorCode.TOKEN_NOT_FOUND, "해당 사용자 id의 refresh token을 찾을 수 없습니다."));

        // 3. DB 값과 쿠키의 refresh token 값 일치하는지 체크
        if (!refreshTokenEntity.getRefreshToken().equals(refreshToken))
            throw new BaseException(ErrorCode.INVALID_TOKEN, "refresh token 값이 올바르지 않습니다.");

        // 4. 새 토큰 발급 + HttpOnly 쿠키로 세팅
        TokenDto tokens = createAndSaveToken(userIdFromToken);
        cookieUtil.addCookie(response, "accessToken", tokens.getAccessToken(),
                (int) (JwtProvider.EXPIRE_ACCESS / 1000), COOKIE_SECURE, COOKIE_SAMESITE);
        cookieUtil.addCookie(response, "refreshToken", tokens.getRefreshToken(),
                (int) (JwtProvider.EXPIRE_REFRESH / 1000), COOKIE_SECURE, COOKIE_SAMESITE);
    }

}
