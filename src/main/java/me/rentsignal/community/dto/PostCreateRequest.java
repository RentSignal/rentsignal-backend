package me.rentsignal.community.dto;

import lombok.Getter;

@Getter
public class PostCreateRequest {

    private String title;
    private String content;
    private String category;

}
