package me.rentsignal.community.dto;

import lombok.Builder;
import lombok.Getter;
import me.rentsignal.community.domain.Post;

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

    public static PostListItemResponse from(Post post) {
        return PostListItemResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .category(post.getCategory())
                .userId(post.getUserId())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .viewCount(post.getViewCount())
                .build();
    }
}