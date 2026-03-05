package me.rentsignal.community.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자
    @Column(nullable = false)
    private Long userId;

    // 제목
    @Column(nullable = false)
    private String title;

    // 내용
    @Column(columnDefinition = "TEXT")
    private String content;

    // 카테고리
    @Column(nullable = false)
    private String category;

    // 좋아요 수
    @Column(nullable = false)
    @Builder.Default
    private Integer likeCount = 0;

    // 조회수
    @Column(nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    // 댓글 수
    @Column(nullable = false)
    @Builder.Default
    private Integer commentCount = 0;

    // 삭제 여부
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    // 생성 시간
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 수정 시간
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}