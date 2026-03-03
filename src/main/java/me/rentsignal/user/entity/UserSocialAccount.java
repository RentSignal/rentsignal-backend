package me.rentsignal.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"provider", "provider_user_id"})}
)
public class UserSocialAccount {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    // 사용자 고유 ID
    @Column(nullable = false, name="provider_user_id")
    private String providerUserId;

    @Builder
    public UserSocialAccount(Provider provider, String providerUserId) {
        this.provider = provider;
        this.providerUserId = providerUserId;
    }

    void setUser(User user) {
        this.user = user;
    }

}
