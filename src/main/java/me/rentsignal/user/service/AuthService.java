package me.rentsignal.user.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.global.security.CookieUtil;
import me.rentsignal.user.entity.Role;
import me.rentsignal.user.entity.User;
import me.rentsignal.user.repository.RefreshTokenRepository;
import me.rentsignal.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${COOKIE_SECURE}")
    private boolean COOKIE_SECURE;

    @Value("${COOKIE_SAMESITE}")
    private String COOKIE_SAMESITE;

    private final UserRepository userRepository;
    private final CookieUtil cookieUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    /** provider로부터 nickname이나 name을 제공받지 않은 경우 임시 닉네임 생성 */
    public String generateTempName() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Transactional
    public void registerPhone(Long userId, String phone) {
        User user = getCurrentUser(userId);
        // phone 등록 + role 변경
        user.registerPhone(phone);
    }

    /** 현재 로그인한 User 객체 가져오기 */
    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void logout(HttpServletResponse response, String refreshToken) {
        // DB에 저장된 refresh token 무효화
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.deleteByRefreshToken(refreshToken);
        }

        // 쿠키 삭제
        cookieUtil.expireCookie(response, "accessToken", COOKIE_SECURE, COOKIE_SAMESITE);
        cookieUtil.expireCookie(response, "refreshToken", COOKIE_SECURE, COOKIE_SAMESITE);
    }

    public User validateUserAccess(Long userId) {
        User user = getCurrentUser(userId);

        if (user.getRole() == Role.ROLE_GUEST)
            throw new BaseException(ErrorCode.FORBIDDEN, "현재 사용자 role - " + user.getRole().name());


        return user;
    }

}
