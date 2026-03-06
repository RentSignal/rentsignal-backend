package me.rentsignal.community.repository;

import me.rentsignal.community.domain.Post;
import me.rentsignal.community.domain.PostLike;
import me.rentsignal.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByPostAndUser(Post post, User user);

}