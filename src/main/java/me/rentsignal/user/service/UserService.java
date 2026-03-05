package me.rentsignal.user.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.user.dto.UserInfoDto;
import me.rentsignal.user.entity.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthService authService;

    public UserInfoDto getUserInfo(Long userId) {
        User user = authService.getCurrentUser(userId);

        return UserInfoDto.builder()
                .name(user.getName())
                .imageUrl(user.getImageUrl()).build();
    }

}
