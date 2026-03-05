package me.rentsignal.community.controller;

import lombok.RequiredArgsConstructor;
import me.rentsignal.community.dto.*;
import me.rentsignal.community.service.CommunityService;
import me.rentsignal.global.response.BaseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;

    // 게시글 목록
    @GetMapping("/posts")
    public ResponseEntity<BaseResponse<Page<PostListItemResponse>>> getPosts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {

        Page<PostListItemResponse> result =
                communityService.getPosts(category, keyword, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success(result));
    }

    // 게시글 상세
    @GetMapping("/posts/{postId}")
    public ResponseEntity<BaseResponse<PostDetailResponse>> getPostDetail(
            @PathVariable Long postId
    ) {

        PostDetailResponse result =
                communityService.getPostDetail(postId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success(result));
    }

    // 게시글 작성
    @PostMapping("/posts")
    public ResponseEntity<BaseResponse<Long>> createPost(
            @RequestBody PostCreateRequest request
    ) {

        Long postId = communityService.createPost(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success("게시글 작성 성공", postId));
    }

    // 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<BaseResponse<Long>> createComment(
            @PathVariable Long postId,
            @RequestBody CommentCreateRequest request
    ) {

        Long commentId =
                communityService.createComment(postId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success("댓글 작성 성공", commentId));
    }

    // 댓글 목록
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<BaseResponse<Page<CommentResponse>>> getComments(
            @PathVariable Long postId,
            Pageable pageable
    ) {

        Page<CommentResponse> result =
                communityService.getComments(postId, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success(result));
    }

    // 게시글 좋아요
    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<BaseResponse<Void>> togglePostLike(
            @PathVariable Long postId
    ) {

        communityService.togglePostLike(postId, 1L);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("좋아요 처리 완료", null));
    }

    // 댓글 좋아요
    @PostMapping("/comments/{commentId}/likes")
    public ResponseEntity<BaseResponse<Void>> toggleCommentLike(
            @PathVariable Long commentId
    ) {

        communityService.toggleCommentLike(commentId, 1L);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("좋아요 처리 완료", null));
    }

    // 게시글 수정
    @PatchMapping("/posts/{postId}")
    public ResponseEntity<BaseResponse<Void>> updatePost(
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request
    ) {

        communityService.updatePost(postId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("게시글 수정 완료", null));
    }

    // 게시글 삭제
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<BaseResponse<Void>> deletePost(
            @PathVariable Long postId
    ) {

        communityService.deletePost(postId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("게시글 삭제 완료", null));
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<BaseResponse<Void>> deleteComment(
            @PathVariable Long commentId
    ) {

        communityService.deleteComment(commentId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("댓글 삭제 완료", null));
    }
}