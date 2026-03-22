package me.rentsignal.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import me.rentsignal.community.domain.Category;

@Getter
@Setter
public class PostCreateRequest {

    @NotNull(message = "카테고리를 선택해주세요.")
    private Category category;

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    private Long neighborhoodId;
}