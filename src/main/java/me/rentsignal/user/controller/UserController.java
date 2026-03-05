package me.rentsignal.user.controller;

import lombok.RequiredArgsConstructor;
import me.rentsignal.global.response.BaseResponse;
import me.rentsignal.global.security.CustomPrincipal;
import me.rentsignal.user.dto.UserInfoDto;
import me.rentsignal.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<BaseResponse<?>> getUserInfo(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        UserInfoDto userInfo = userService.getUserInfo(customPrincipal.getId());
        return ResponseEntity
                .ok(BaseResponse.success("사용자 정보를 정상적으로 불러왔습니다.", userInfo));
    }

}
