package me.rentsignal.community.dto;

import lombok.Builder;
import lombok.Getter;
import me.rentsignal.community.domain.Comment;

@Getter
@Builder
public class CommentResponse {

    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private Integer likeCount;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .build();
    }
}
