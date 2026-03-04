package me.rentsignal.community.dto;

import java.time.LocalDateTime;

public record PostListItemResponse(
        Long id,
        String category,
        String title,
        int viewCount,
        int likeCount,
        int commentCount,
        LocalDateTime createdAt
) {}