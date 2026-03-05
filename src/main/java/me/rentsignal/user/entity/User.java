package me.rentsignal.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import me.rentsignal.global.entity.BaseTimeEntity;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, length = 11)
    @Pattern(
            regexp = "^010\\d{8}$",
            message = "전화번호는 하이픈 없이 010으로 시작하는 11자리여야합니다."
    )
    private String phone;

    @Column(nullable = false)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_GUEST;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private List<UserSocialAccount> socialAccounts = new ArrayList<>();

    @Builder
    public User(String name, String phone, Role role, String imageUrl) {
        this.name = name;
        this.phone = phone;
        this.role = (role == null) ? Role.ROLE_GUEST : role;
        this.imageUrl = imageUrl;
    }

    public void registerPhone(String phone) {
        this.phone = phone;
        this.role = Role.ROLE_USER;
    }

    public void addRefreshToken(RefreshToken refreshToken) {
        if (refreshToken.getUser() == null
            || !this.equals(refreshToken.getUser())) {
            // refresh token에 지정한 user와 연결하려는 user가 일치하지 않는 경우
            throw new BaseException(ErrorCode.USER_MISMATCH_INTERNAL);
        }
        this.refreshTokens.add(refreshToken);
    }

    public void addSocialAccount(UserSocialAccount socialAccount) {
        this.socialAccounts.add(socialAccount);
        socialAccount.setUser(this);
    }

    /** 소셜 계정을 다른 user로 이관 */
    public void moveSocialAccount(UserSocialAccount socialAccount, User newUser) {
        if (!this.socialAccounts.contains(socialAccount))
            throw new BaseException(ErrorCode.SOCIAL_ACCOUNT_NOT_OWNED);

        this.socialAccounts.remove(socialAccount);
        newUser.addSocialAccount(socialAccount);
    }

}
