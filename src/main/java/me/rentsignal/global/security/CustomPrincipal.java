package me.rentsignal.global.security;

import lombok.Builder;
import lombok.Getter;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.user.entity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class CustomPrincipal implements OAuth2User, UserDetails {

    private final Long id;
    private final Role role;
    private final Map<String, Object> attributes;

    private CustomPrincipal(Long id, Role role, Map<String, Object> attributes) {
        if (id == null) throw new BaseException(ErrorCode.INVALID_PRINCIPAL, "id 값이 null입니다.");
        if (role == null) throw new BaseException(ErrorCode.INVALID_PRINCIPAL, "role이 null입니다.");
        this.id = id;
        this.role = role;
        this.attributes = (attributes == null) ? Map.of() : attributes;
    }

    /** JWT용 생성 메서드 */
    public static CustomPrincipal fromJwt(Long id, Role role) {
        return new CustomPrincipal(id, role, Map.of());
    }

    /** OAuth용 생성 메서드 */
    public static CustomPrincipal fromOAuth(Long id, Role role, Map<String, Object> attributes) {
        return new CustomPrincipal(id, role, attributes);
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return String.valueOf(id);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }

}
