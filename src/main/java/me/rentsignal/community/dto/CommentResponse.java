package me.rentsignal.community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;

}
