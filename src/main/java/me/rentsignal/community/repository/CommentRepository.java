package me.rentsignal.community.repository;

import me.rentsignal.community.domain.Comment;
import me.rentsignal.community.domain.Post;
import me.rentsignal.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(Long postId, Pageable pageable);

    Page<Comment> findByUser(User user, Pageable pageable);

    @Query("""
        select distinct c.post
        from Comment c
        where c.user = :user
          and c.isDeleted = false
          and c.post.isDeleted = false
    """)
    Page<Post> findPostsByUser(@Param("user") User user, Pageable pageable);
}