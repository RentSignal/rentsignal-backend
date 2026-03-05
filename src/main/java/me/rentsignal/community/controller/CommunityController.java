package me.rentsignal.community.controller;

import lombok.RequiredArgsConstructor;
import me.rentsignal.community.dto.*;
import me.rentsignal.community.service.CommunityService;
import me.rentsignal.global.security.CustomPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping("/posts")
    public Page<PostListItemResponse> getPosts(
            @AuthenticationPrincipal CustomPrincipal user,
            @RequestParam(required=false) String category,
            @RequestParam(required=false) String keyword,
            @RequestParam(defaultValue="latest") String sort,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size
    ){
        return communityService.getPosts(user.getId(),category,keyword,sort,page,size);
    }

    @PostMapping("/posts")
    public Long createPost(
            @AuthenticationPrincipal CustomPrincipal user,
            @RequestBody PostCreateRequest request
    ){
        return communityService.createPost(user.getId(),request);
    }

    @PostMapping("/posts/{postId}/comments")
    public Long createComment(
            @AuthenticationPrincipal CustomPrincipal user,
            @PathVariable Long postId,
            @RequestBody CommentCreateRequest request
    ){
        return communityService.createComment(postId,user.getId(),request);
    }

    @PostMapping("/posts/{postId}/likes")
    public void likePost(
            @AuthenticationPrincipal CustomPrincipal user,
            @PathVariable Long postId
    ){
        communityService.likePost(postId,user.getId());
    }

    @PostMapping("/comments/{commentId}/likes")
    public void likeComment(
            @AuthenticationPrincipal CustomPrincipal user,
            @PathVariable Long commentId
    ){
        communityService.likeComment(commentId,user.getId());
    }

}