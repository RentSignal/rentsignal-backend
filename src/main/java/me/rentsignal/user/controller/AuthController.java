package me.rentsignal.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.rentsignal.global.response.BaseResponse;
import me.rentsignal.global.security.CustomPrincipal;
import me.rentsignal.user.dto.PhoneDto;
import me.rentsignal.user.service.AuthService;
import me.rentsignal.user.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final TokenService tokenService;
    private final AuthService authService;

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@CookieValue(value = "refreshToken", required = false) String refreshToken,
                                     HttpServletResponse response) {
        tokenService.reissue(refreshToken, response);
        return ResponseEntity.ok(BaseResponse.success(null));
    }

    @PostMapping("/phone")
    public ResponseEntity<?> registerPhone(@Valid @RequestBody PhoneDto phone,
                                           @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        authService.registerPhone(customPrincipal.getId(), phone.getPhone());
        return ResponseEntity.ok(BaseResponse.success(null));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response,
                                    @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        authService.logout(response, refreshToken);
        return ResponseEntity.ok(BaseResponse.success(null));
    }

}
