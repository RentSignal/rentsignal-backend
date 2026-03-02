package me.rentsignal.global.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    /** 쿠키 세팅 */
    public void addCookie(HttpServletResponse response,
                          String name, String value,
                          int maxAgeSeconds, boolean secure, String sameSite) {
        String cookie = name + "=" + value
                + "; Max-Age=" + maxAgeSeconds
                + "; Path=/"
                + "; HttpOnly"
                + (secure ? "; Secure" : "")
                + "; SameSite=" + sameSite;

        response.addHeader("Set-Cookie", cookie);
    }

}
