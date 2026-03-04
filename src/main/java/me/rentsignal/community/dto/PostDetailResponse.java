package me.rentsignal.community.dto;

import java.time.LocalDateTime;

public record PostDetailResponse(
        Long id,
        Long userId,
        Long neighborhoodId,
        String category,
        String title,
        String content,
        int viewCount,
        int likeCount,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
