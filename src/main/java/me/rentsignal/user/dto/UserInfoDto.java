package me.rentsignal.user.dto;

import lombok.Builder;
import lombok.Data;
import me.rentsignal.user.entity.Role;

@Data
@Builder
public class UserInfoDto {

    public String name;

    public String imageUrl;

    public Role role;

}
