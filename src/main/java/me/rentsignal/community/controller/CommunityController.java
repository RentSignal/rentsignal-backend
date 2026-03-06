package me.rentsignal.community.controller;

import lombok.RequiredArgsConstructor;
import me.rentsignal.community.dto.*;
import me.rentsignal.community.service.CommunityService;
import me.rentsignal.global.response.BaseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;

    // 게시글 목록 조회
    @GetMapping("/posts")
    public BaseResponse<Page<PostListItemResponse>> getPosts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {

        return BaseResponse.success(
                communityService.getPosts(category, keyword, pageable)
        );
    }

    // 게시글 상세 조회
    @GetMapping("/posts/{postId}")
    public BaseResponse<PostDetailResponse> getPostDetail(
            @PathVariable Long postId
    ) {

        return BaseResponse.success(
                communityService.getPostDetail(postId)
        );
    }

    // 게시글 작성
    @PostMapping("/posts")
    public BaseResponse<Long> createPost(
            @RequestBody PostCreateRequest request
    ) {

        Long postId = communityService.createPost(1L, request); // 테스트용 userId

        return BaseResponse.success(postId);
    }

    // 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    public BaseResponse<Long> createComment(
            @PathVariable Long postId,
            @RequestBody CommentCreateRequest request
    ) {

        Long commentId =
                communityService.createComment(1L, postId, request); // 테스트용 userId

        return BaseResponse.success(commentId);
    }

    // 댓글 조회
    @GetMapping("/posts/{postId}/comments")
    public BaseResponse<Page<CommentResponse>> getComments(
            @PathVariable Long postId,
            Pageable pageable
    ) {

        return BaseResponse.success(
                communityService.getComments(postId, pageable)
        );
    }

    // 게시글 좋아요
    @PostMapping("/posts/{postId}/like")
    public BaseResponse<Void> togglePostLike(
            @PathVariable Long postId
    ) {

        communityService.togglePostLike(1L, postId); // 테스트용 userId

        return BaseResponse.success(null);
    }

    // 댓글 좋아요
    @PostMapping("/comments/{commentId}/like")
    public BaseResponse<Void> toggleCommentLike(
            @PathVariable Long commentId
    ) {

        communityService.toggleCommentLike(1L, commentId); // 테스트용 userId

        return BaseResponse.success(null);
    }

}