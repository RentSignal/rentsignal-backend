package me.rentsignal.community.repository;

import me.rentsignal.community.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike,Long> {

    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

}
