package me.rentsignal.community.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long userId,
        String content,
        LocalDateTime createdAt
) {}
