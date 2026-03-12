package me.rentsignal.community.dto;

import lombok.Builder;
import lombok.Getter;
import me.rentsignal.community.domain.Post;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostListItemResponse {

    private Long id;
    private String title;
    private String category;
    private Long userId;
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;
    private LocalDateTime createdAt;

    public static PostListItemResponse from(Post post) {
        return PostListItemResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .category(post.getCategory())
                .userId(post.getUser().getId())   // 수정
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build();
    }
}