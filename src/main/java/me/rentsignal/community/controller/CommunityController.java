package me.rentsignal.community.controller;

import me.rentsignal.community.dto.*;
import me.rentsignal.community.service.CommunityService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    // 게시글 목록
    @GetMapping("/posts")
    public Page<PostListItemResponse> getPosts(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return communityService.getPosts(categoryId, keyword, sort, page, size);
    }

    // 게시글 상세
    @GetMapping("/posts/{postId}")
    public PostDetailResponse getPostDetail(@PathVariable Long postId) {
        return communityService.getPostDetail(postId);
    }

    // 댓글 조회
    @GetMapping("/posts/{postId}/comments")
    public Page<CommentResponse> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return communityService.getComments(postId, page, size);
    }

    //  게시글 작성
    @PostMapping("/posts")
    public Long createPost(@RequestBody PostCreateRequest request) {
        return communityService.createPost(request);
    }
}