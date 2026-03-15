package me.rentsignal.community.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCreateRequest {

    private String category;
    private String title;
    private String content;
    private Long neighborhoodId;

}