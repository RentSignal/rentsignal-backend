package me.rentsignal.community.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.community.domain.*;
import me.rentsignal.community.dto.*;
import me.rentsignal.community.repository.*;
import me.rentsignal.user.entity.User;
import me.rentsignal.user.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<PostListItemResponse> getPosts(Long userId, String category, String keyword, String sort, int page, int size) {

        User user = userRepository.findById(userId).orElseThrow();

        Sort s = "popular".equalsIgnoreCase(sort)
                ? Sort.by(Sort.Direction.DESC, "likeCount")
                .and(Sort.by(Sort.Direction.DESC, "createdAt"))
                : Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable pageable = PageRequest.of(page, size, s);

        return postRepository.search(
                category,
                keyword,
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

    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(Long postId){

        Post post = postRepository.findById(postId).orElseThrow();

        post.setViewCount(post.getViewCount() + 1);

        return new PostDetailResponse(
                post.getId(),
                post.getUserId(),
                post.getCategory(),
                post.getTitle(),
                post.getContent(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getCommentCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    @Transactional
    public Long createPost(Long userId, PostCreateRequest request) {

        User user = userRepository.findById(userId).orElseThrow();

        Post post = Post.builder()
                .userId(userId)
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        return postRepository.save(post).getId();
    }

    @Transactional
    public Long createComment(Long postId, Long userId, CommentCreateRequest request) {

        Post post = postRepository.findById(postId).orElseThrow();

        Comment comment = Comment.builder()
                .postId(postId)
                .userId(userId)
                .content(request.getContent())
                .build();

        post.setCommentCount(post.getCommentCount() + 1);

        return commentRepository.save(comment).getId();
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(Long postId, int page, int size){

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());

        return commentRepository
                .findByPostIdAndIsDeletedFalse(postId, pageable)
                .map(c -> new CommentResponse(
                        c.getId(),
                        c.getUserId(),
                        c.getContent(),
                        c.getCreatedAt()
                ));
    }

    @Transactional
    public void likePost(Long postId, Long userId){

        if(postLikeRepository.findByPostIdAndUserId(postId, userId).isPresent()){
            throw new IllegalStateException("already liked");
        }

        PostLike like = PostLike.builder()
                .postId(postId)
                .userId(userId)
                .build();

        postLikeRepository.save(like);

        Post post = postRepository.findById(postId).orElseThrow();
        post.setLikeCount(post.getLikeCount() + 1);
    }

    @Transactional
    public void likeComment(Long commentId, Long userId){

        if(commentLikeRepository.findByCommentIdAndUserId(commentId, userId).isPresent()){
            throw new IllegalStateException("already liked");
        }

        CommentLike like = CommentLike.builder()
                .commentId(commentId)
                .userId(userId)
                .build();

        commentLikeRepository.save(like);

        Comment comment = commentRepository.findById(commentId).orElseThrow();
        comment.setLikeCount(comment.getLikeCount() + 1);
    }

}