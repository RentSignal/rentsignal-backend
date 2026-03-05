package me.rentsignal.user.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserInfoUpdateRequestDto {

    private String name;

    @Pattern(
            regexp = "^010\\d{8}$",
            message = "전화번호는 하이픈 없이 010으로 시작하는 11자리여야합니다."
    )
    private String phoneNum;

    private MultipartFile image;

}
