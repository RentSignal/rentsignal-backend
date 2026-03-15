package me.rentsignal.community.repository;

import me.rentsignal.community.domain.Comment;
import me.rentsignal.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(Long postId, Pageable pageable);
    Page<Comment> findByUser(User user, Pageable pageable);

}