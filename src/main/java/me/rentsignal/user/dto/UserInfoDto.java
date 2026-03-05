package me.rentsignal.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoDto {

    public String name;

    public String imageUrl;

}
