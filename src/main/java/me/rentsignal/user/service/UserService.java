package me.rentsignal.user.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.user.dto.UserInfoDto;
import me.rentsignal.user.dto.UserInfoUpdateRequestDto;
import me.rentsignal.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthService authService;

    public UserInfoDto getUserInfo(Long userId) {
        User user = authService.getCurrentUser(userId);

        return UserInfoDto.builder()
                .name(user.getName())
                .role(user.getRole())
                .imageUrl(user.getImageUrl()).build();
    }

    @Transactional
    public void updateUserInfo(Long userId, UserInfoUpdateRequestDto updateDto) {
        User user = authService.getCurrentUser(userId);

        String newName = updateDto.getName();
        String newPhoneNum = updateDto.getPhoneNum();
        MultipartFile newImage = updateDto.getImage();

        if (newName != null && !newName.isBlank())
            user.updateName(newName);
        if (newPhoneNum != null && !newPhoneNum.isBlank())
            user.registerPhone(newPhoneNum);
        if (newImage != null && !newImage.isEmpty()) {
            // TODO : S3 연동 후 업로드하고 url 저장
            user.updateImageUrl(newImage.getOriginalFilename());
        }
    }

}
