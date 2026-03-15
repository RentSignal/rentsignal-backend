package me.rentsignal.community.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.community.domain.*;
import me.rentsignal.community.dto.*;
import me.rentsignal.community.repository.*;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.global.security.CustomPrincipal;
import me.rentsignal.location.entity.Neighborhood;
import me.rentsignal.location.repository.NeighborhoodRepository;
import me.rentsignal.user.entity.Role;
import me.rentsignal.user.entity.User;
import me.rentsignal.user.service.AuthService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final NeighborhoodRepository neighborhoodRepository;
    private final AuthService authService;

    // 커뮤니티 권한 체크
    private User validateCommunityAccess(CustomPrincipal principal) {

        Long userId = principal.getId();

        User user = authService.getCurrentUser(userId);

        if (user.getRole() == Role.ROLE_GUEST) {
            throw new BaseException(ErrorCode.FORBIDDEN, user.getRole().name());
        }

        return user;
    }

    // 게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<PostListItemResponse> getPosts(String category, Long neighborhoodId, Pageable pageable) {

        return postRepository.search(category, neighborhoodId, pageable)
                .map(PostListItemResponse::from);
    }

    // 게시글 상세 조회
    @Transactional
    public PostDetailResponse getPostDetail(Long postId, CustomPrincipal principal) {

        User user = validateCommunityAccess(principal);

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.INVALID_INPUT_VALUE, "게시글을 찾을 수 없습니다.")
                );

        if (post.getIsDeleted()) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "삭제된 게시글입니다.");
        }

        post.increaseViewCount();

        return PostDetailResponse.from(post, user.getRole());
    }

    // 게시글 작성
    @Transactional
    public Long createPost(PostCreateRequest request, CustomPrincipal principal) {

        User user = validateCommunityAccess(principal);

        Neighborhood neighborhood =
                neighborhoodRepository.findById(request.getNeighborhoodId())
                        .orElseThrow(() ->
                                new BaseException(ErrorCode.INVALID_INPUT_VALUE, "동네를 찾을 수 없습니다.")
                        );

        Post post = Post.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .neighborhood(neighborhood)
                .build();

        postRepository.save(post);

        return post.getId();
    }

    // 댓글 작성
    @Transactional
    public Long createComment(Long postId, CommentCreateRequest request, CustomPrincipal principal) {

        User user = validateCommunityAccess(principal);

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.INVALID_INPUT_VALUE, "게시글을 찾을 수 없습니다.")
                );

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(request.getContent())
                .build();

        commentRepository.save(comment);

        post.increaseCommentCount();

        return comment.getId();
    }

    // 댓글 조회
    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(Long postId, Pageable pageable, CustomPrincipal principal) {

        validateCommunityAccess(principal);

        return commentRepository
                .findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(postId, pageable)
                .map(CommentResponse::from);
    }

    // 게시글 좋아요
    @Transactional
    public void togglePostLike(Long postId, CustomPrincipal principal) {

        User user = validateCommunityAccess(principal);

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.INVALID_INPUT_VALUE, "게시글을 찾을 수 없습니다.")
                );

        Optional<PostLike> like =
                postLikeRepository.findByPostAndUser(post, user);

        if (like.isPresent()) {

            postLikeRepository.delete(like.get());
            post.decreaseLikeCount();

        } else {

            PostLike postLike = PostLike.builder()
                    .post(post)
                    .user(user)
                    .build();

            postLikeRepository.save(postLike);
            post.increaseLikeCount();
        }
    }

    // 댓글 좋아요
    @Transactional
    public void toggleCommentLike(Long commentId, CustomPrincipal principal) {

        User user = validateCommunityAccess(principal);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.INVALID_INPUT_VALUE, "댓글을 찾을 수 없습니다.")
                );

        Optional<CommentLike> like =
                commentLikeRepository.findByCommentAndUser(comment, user);

        if (like.isPresent()) {

            commentLikeRepository.delete(like.get());
            comment.decreaseLikeCount();

        } else {

            CommentLike commentLike = CommentLike.builder()
                    .comment(comment)
                    .user(user)
                    .build();

            commentLikeRepository.save(commentLike);
            comment.increaseLikeCount();
        }
    }

    // 게시글 수정
    @Transactional
    public void updatePost(Long postId, PostUpdateRequest request, CustomPrincipal principal) {

        User user = validateCommunityAccess(principal);

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.INVALID_INPUT_VALUE, "게시글을 찾을 수 없습니다.")
                );

        if (!post.getUser().getId().equals(user.getId())) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "작성자만 수정할 수 있습니다.");
        }

        post.update(request.getTitle(), request.getContent());
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, CustomPrincipal principal) {

        User user = validateCommunityAccess(principal);

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.INVALID_INPUT_VALUE, "게시글을 찾을 수 없습니다.")
                );

        if (!post.getUser().getId().equals(user.getId())) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "작성자만 삭제할 수 있습니다.");
        }

        post.softDelete();
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, CustomPrincipal principal) {

        User user = validateCommunityAccess(principal);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.INVALID_INPUT_VALUE, "댓글을 찾을 수 없습니다.")
                );

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "작성자만 삭제할 수 있습니다.");
        }

        comment.softDelete();

        Post post = comment.getPost();
        post.decreaseCommentCount();
    }

    // 내가 쓴 게시글
    @Transactional(readOnly = true)
    public Page<PostListItemResponse> getMyPosts(CustomPrincipal principal, Pageable pageable) {

        User user = authService.getCurrentUser(principal.getId());

        return postRepository
                .findByUser(user, pageable)
                .map(PostListItemResponse::from);
    }

    // 내가 쓴 댓글
    @Transactional(readOnly = true)
    public Page<CommentResponse> getMyComments(CustomPrincipal principal, Pageable pageable) {

        User user = authService.getCurrentUser(principal.getId());

        return commentRepository
                .findByUser(user, pageable)
                .map(CommentResponse::from);
    }

    // 내가 좋아요한 글
    @Transactional(readOnly = true)
    public Page<PostListItemResponse> getMyLikedPosts(CustomPrincipal principal, Pageable pageable) {

        User user = authService.getCurrentUser(principal.getId());

        return postLikeRepository
                .findByUser(user, pageable)
                .map(postLike -> PostListItemResponse.from(postLike.getPost()));
    }
}