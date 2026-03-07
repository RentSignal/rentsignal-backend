package me.rentsignal.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import me.rentsignal.user.dto.TokenDto;
import me.rentsignal.user.service.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인 성공 후 JWT 발급 + redirect
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${COOKIE_SECURE}")
    private boolean COOKIE_SECURE;

    @Value("${COOKIE_SAMESITE}")
    private String COOKIE_SAMESITE;

    private final CookieUtil cookieUtil;
    private final TokenService tokenService;

    @Value("${REDIRECT_URI}")
    private String REDIRECT_URI;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1. principal 추출
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();

        // 2. JWT 생성
        TokenDto tokens = tokenService.createAndSaveToken(principal.getId());

        // 3. HttpOnly 쿠키로 토큰 내려주기
        // TODO : 배포 시 Secure = true, SameSite = None으로 환경변수 값 변경 필요
        cookieUtil.addCookie(response, "accessToken", tokens.getAccessToken(),
                (int) (JwtProvider.EXPIRE_ACCESS / 1000), COOKIE_SECURE, COOKIE_SAMESITE);
        cookieUtil.addCookie(response, "refreshToken", tokens.getRefreshToken(),
                (int) (JwtProvider.EXPIRE_REFRESH / 1000), COOKIE_SECURE, COOKIE_SAMESITE);

        // 4. OAuth2 로그인 과정에서 생긴 세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null)
            session.invalidate();

        cookieUtil.expireCookie(response, "JSESSIONID", COOKIE_SECURE, COOKIE_SAMESITE);

        // 5. redirect
        getRedirectStrategy().sendRedirect(request, response, REDIRECT_URI);
    }

}
