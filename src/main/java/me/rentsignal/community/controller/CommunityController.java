package me.rentsignal.community.controller;

import lombok.RequiredArgsConstructor;
import me.rentsignal.community.dto.*;
import me.rentsignal.community.service.CommunityService;
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
    public Page<PostListItemResponse> getPosts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        return communityService.getPosts(category, keyword, pageable);
    }

    // 게시글 상세 조회
    @GetMapping("/posts/{postId}")
    public PostDetailResponse getPostDetail(@PathVariable Long postId) {
        return communityService.getPostDetail(postId);
    }

    // 게시글 작성
    @PostMapping("/posts")
    public Long createPost(@RequestBody PostCreateRequest request) {
        return communityService.createPost(request);
    }

    // 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    public Long createComment(
            @PathVariable Long postId,
            @RequestBody CommentCreateRequest request
    ) {
        return communityService.createComment(postId, request);
    }

    // 댓글 목록 조회
    @GetMapping("/posts/{postId}/comments")
    public Page<CommentResponse> getComments(
            @PathVariable Long postId,
            Pageable pageable
    ) {
        return communityService.getComments(postId, pageable);
    }

    // 게시글 좋아요 토글
    @PostMapping("/posts/{postId}/like")
    public void togglePostLike(@PathVariable Long postId) {
        Long userId = 1L; // 테스트용
        communityService.togglePostLike(postId, userId);
    }

    // 댓글 좋아요 토글
    @PostMapping("/comments/{commentId}/like")
    public void toggleCommentLike(@PathVariable Long commentId) {
        Long userId = 1L; // 테스트용
        communityService.toggleCommentLike(commentId, userId);
    }

}