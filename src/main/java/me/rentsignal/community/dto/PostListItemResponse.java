package me.rentsignal.community.dto;

import lombok.Builder;
import lombok.Getter;
import me.rentsignal.community.domain.Post;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostListItemResponse {

    private static final int PREVIEW_LENGTH = 50;

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
    private LocalDateTime createdAt;

    public static PostListItemResponse from(Post post) {
        return PostListItemResponse.builder()
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
                .build();
    }
    private static String makePreview(String content) {
        if (content == null) return null;

        return content.length() > PREVIEW_LENGTH
                ? content.substring(0, PREVIEW_LENGTH) + "..."
                : content;
    }
}