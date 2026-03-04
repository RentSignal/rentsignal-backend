package me.rentsignal.community.service;

import me.rentsignal.community.domain.*;
import me.rentsignal.community.dto.*;
import me.rentsignal.community.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public CommunityService(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    // 게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<PostListItemResponse> getPosts(String category, String keyword, String sort, int page, int size) {

        Sort s = "popular".equalsIgnoreCase(sort)
                ? Sort.by(Sort.Direction.DESC, "likeCount").and(Sort.by(Sort.Direction.DESC, "createdAt"))
                : Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable pageable = PageRequest.of(page, size, s);

        return postRepository.search(
                emptyToNull(category),
                emptyToNull(keyword),
                pageable
        ).map(p -> new PostListItemResponse(
                p.getId(),
                p.getCategory(),
                p.getTitle(),
                p.getViewCount(),
                p.getLikeCount(),
                p.getCommentCount(),
                p.getCreatedAt()
        ));
    }

    // 게시글 상세 조회
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(Long postId) {
        Post p = postRepository.findById(postId)
                .filter(post -> !post.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("post not found: " + postId));

        return new PostDetailResponse(
                p.getId(),
                p.getUserId(),
                p.getNeighborhoodId(),
                p.getCategory(),
                p.getTitle(),
                p.getContent(),
                p.getViewCount(),
                p.getLikeCount(),
                p.getCommentCount(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    // 댓글 조회
    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        return commentRepository.findByPostIdAndIsDeletedFalse(postId, pageable)
                .map(c -> new CommentResponse(
                        c.getId(),
                        c.getUserId(),
                        c.getContent(),
                        c.getCreatedAt()
                ));
    }

    //  게시글 작성
    @Transactional
    public Long createPost(PostCreateRequest request) {

        Post post = Post.builder()
                .userId(request.getUserId())
                .neighborhoodId(request.getNeighborhoodId())
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .viewCount(0)
                .likeCount(0)
                .commentCount(0)
                .isDeleted(false)
                .build();

        Post saved = postRepository.save(post);

        return saved.getId();
    }

    private String emptyToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}