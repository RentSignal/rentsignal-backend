package me.rentsignal.user.repository;

import me.rentsignal.user.entity.Provider;
import me.rentsignal.user.entity.UserSocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSocialAccountRepository extends JpaRepository<UserSocialAccount, Long> {
    Optional<UserSocialAccount> findByProviderAndProviderUserId(Provider provider, String providerId);
}
