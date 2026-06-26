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
    private Boolean isLiked;

    public static PostDetailResponse from(Post post, Role role, Boolean isLiked) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory().getLabel())
                .userId(post.getUser().getId())
                .userName(post.getUser().getName())
                .neighborhoodName(
                        post.getNeighborhood() != null ? post.getNeighborhood().getName() : null
                )
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .role(role.name())
                .isLiked(isLiked)
                .build();
    }
}