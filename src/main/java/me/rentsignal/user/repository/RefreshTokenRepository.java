package me.rentsignal.user.repository;

import me.rentsignal.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUser_Id(Long userId);
    void deleteByRefreshToken(String refreshToken);
}
