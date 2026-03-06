package me.rentsignal.community.repository;

import me.rentsignal.community.domain.Comment;
import me.rentsignal.community.domain.CommentLike;
import me.rentsignal.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByCommentAndUser(Comment comment, User user);

}