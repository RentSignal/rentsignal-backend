package me.rentsignal.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.user.entity.Role;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            try {
                // 토큰 파싱 + 유효성 체크
                Long id = jwtProvider.getUserIdFromToken(token);
                Role role = jwtProvider.getRoleFromToken(token);

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Principal 생성
                    CustomPrincipal customPrincipal = CustomPrincipal.fromJwt(id, role);

                    // Authentication 만들어 Security Context에 세팅
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(customPrincipal, null, customPrincipal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (BadCredentialsException e) {
                SecurityContextHolder.clearContext();
                request.setAttribute("exceptionCode", ErrorCode.INVALID_TOKEN);
                request.setAttribute("exceptionMessage", e.getMessage());

            } catch (CredentialsExpiredException e) {
                SecurityContextHolder.clearContext();
                request.setAttribute("exceptionCode", ErrorCode.EXPIRED_TOKEN);
                request.setAttribute("exceptionMessage", e.getMessage());

            } catch (Exception e) {
                log.error("토큰 처리 예외" + e.getMessage());
                SecurityContextHolder.clearContext();
                request.setAttribute("exceptionCode", ErrorCode.INVALID_TOKEN);
                request.setAttribute("exceptionMessage", "토큰 처리 중 오류가 발생했습니다.");
            }
        }
        filterChain.doFilter(request, response);
    }

    /** 토큰 파싱 **/
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // Authorization 헤더가 없는 경우 Cookie에서 accessToken 찾기
        if (request.getCookies() == null) return null;

        for (var cookie : request.getCookies()) {
            if ("accessToken".equals(cookie.getName()))
                return cookie.getValue();
        }

        return null;
    }

    /** OAuth2 관련 요청은 JWT와 무관하기 때문에 filter X */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest httpServletRequest) {
        String requestURI = httpServletRequest.getRequestURI();
        return requestURI.startsWith("/oauth2/")
                || requestURI.startsWith("/login");
    }

}
