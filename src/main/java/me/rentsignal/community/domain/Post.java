package me.rentsignal.community.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="neighborhood_id", nullable=false)
    private Long neighborhoodId;

    @Column(nullable=false, length=30)
    private String category;

    @Column(nullable=false, length=150)
    private String title;

    @Lob
    @Column(nullable=false)
    private String content;

    @Column(name="view_count", nullable=false)
    private int viewCount;

    @Column(name="like_count", nullable=false)
    private int likeCount;

    @Column(name="comment_count", nullable=false)
    private int commentCount;

    @Column(name="is_deleted", nullable=false)
    private boolean isDeleted;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;
}
