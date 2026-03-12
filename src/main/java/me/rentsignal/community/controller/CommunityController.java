package me.rentsignal.community.controller;

import lombok.RequiredArgsConstructor;
import me.rentsignal.community.dto.*;
import me.rentsignal.community.service.CommunityService;
import me.rentsignal.global.response.BaseResponse;
import me.rentsignal.global.security.CustomPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;

    // 게시글 목록 조회 (ROLE_GUEST도 가능)
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
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomPrincipal principal
    ) {

        return BaseResponse.success(
                communityService.getPostDetail(postId, principal)
        );
    }

    // 게시글 작성
    @PostMapping("/posts")
    public BaseResponse<Long> createPost(
            @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal CustomPrincipal principal
    ) {

        Long postId = communityService.createPost(request, principal);

        return BaseResponse.success(postId);
    }

    // 게시글 수정
    @PatchMapping("/posts/{postId}")
    public BaseResponse<Void> updatePost(
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal CustomPrincipal principal
    ) {

        communityService.updatePost(postId, request, principal);

        return BaseResponse.success(null);
    }

    // 게시글 삭제
    @DeleteMapping("/posts/{postId}")
    public BaseResponse<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomPrincipal principal
    ) {

        communityService.deletePost(postId, principal);

        return BaseResponse.success(null);
    }

    // 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    public BaseResponse<Long> createComment(
            @PathVariable Long postId,
            @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal CustomPrincipal principal
    ) {

        Long commentId =
                communityService.createComment(postId, request, principal);

        return BaseResponse.success(commentId);
    }

    // 댓글 목록 조회
    @GetMapping("/posts/{postId}/comments")
    public BaseResponse<Page<CommentResponse>> getComments(
            @PathVariable Long postId,
            Pageable pageable,
            @AuthenticationPrincipal CustomPrincipal principal
    ) {

        return BaseResponse.success(
                communityService.getComments(postId, pageable, principal)
        );
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public BaseResponse<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomPrincipal principal
    ) {

        communityService.deleteComment(commentId, principal);

        return BaseResponse.success(null);
    }

    // 게시글 좋아요
    @PostMapping("/posts/{postId}/likes")
    public BaseResponse<Void> togglePostLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomPrincipal principal
    ) {

        communityService.togglePostLike(postId, principal);

        return BaseResponse.success(null);
    }

    // 댓글 좋아요
    @PostMapping("/comments/{commentId}/likes")
    public BaseResponse<Void> toggleCommentLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomPrincipal principal
    ) {

        communityService.toggleCommentLike(commentId, principal);

        return BaseResponse.success(null);
    }
    // 내가 쓴 게시글 조회
    @GetMapping("/mypage/posts")
    public BaseResponse<Page<PostListItemResponse>> getMyPosts(
            @AuthenticationPrincipal CustomPrincipal principal,
            Pageable pageable
    ) {

        return BaseResponse.success(
                communityService.getMyPosts(principal, pageable)
        );
    }

    // 내가 쓴 댓글 조회
    @GetMapping("/mypage/comments")
    public BaseResponse<Page<CommentResponse>> getMyComments(
            @AuthenticationPrincipal CustomPrincipal principal,
            Pageable pageable
    ) {

        return BaseResponse.success(
                communityService.getMyComments(principal, pageable)
        );
    }

    // 내가 좋아요한 게시글 조회
    @GetMapping("/mypage/likes")
    public BaseResponse<Page<PostListItemResponse>> getMyLikedPosts(
            @AuthenticationPrincipal CustomPrincipal principal,
            Pageable pageable
    ) {

        return BaseResponse.success(
                communityService.getMyLikedPosts(principal, pageable)
        );
    }

}