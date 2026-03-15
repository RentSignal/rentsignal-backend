package me.rentsignal.community.dto;

import lombok.Builder;
import lombok.Getter;
import me.rentsignal.community.domain.Post;
import me.rentsignal.user.entity.Role;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostDetailResponse {

    private Long id;
    private String title;
    private String content;
    private String category;
    private Long userId;
    private String userName;
    private String neighborhoodName;
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostDetailResponse from(Post post, Role role) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .userId(post.getUser().getId())
                .userName(post.getUser().getName())
                .neighborhoodName(post.getNeighborhood().getName())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .role(role.name())
                .build();
    }
}