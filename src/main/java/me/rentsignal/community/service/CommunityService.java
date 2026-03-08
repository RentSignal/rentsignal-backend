package me.rentsignal.community.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.community.domain.*;
import me.rentsignal.community.dto.*;
import me.rentsignal.community.repository.*;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.global.security.CustomPrincipal;
import me.rentsignal.user.entity.Role;
import me.rentsignal.user.entity.User;
import me.rentsignal.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    // ROLE 체크
    private void validateCommunityAccess(CustomPrincipal principal) {

        if (principal.getRole() == Role.ROLE_GUEST) {
            throw new BaseException(ErrorCode.FORBIDDEN, principal.getRole().name());
        }
    }

    // 로그인 유저 조회
    private User getUser(CustomPrincipal principal) {

        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
    }

    // 게시글 목록 조회 (ROLE_GUEST도 가능)
    @Transactional(readOnly = true)
    public Page<PostListItemResponse> getPosts(String category, String keyword, Pageable pageable) {

        return postRepository.search(category, keyword, pageable)
                .map(PostListItemResponse::from);
    }

    // 게시글 상세 조회
    @Transactional
    public PostDetailResponse getPostDetail(Long postId, CustomPrincipal principal) {

        validateCommunityAccess(principal);

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.INVALID_INPUT_VALUE, "게시글을 찾을 수 없습니다.")
                );

        if (post.getIsDeleted()) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "삭제된 게시글입니다.");
        }

        post.increaseViewCount();

        return PostDetailResponse.from(post, principal.getRole());
    }

    // 게시글 작성
    @Transactional
    public Long createPost(PostCreateRequest request, CustomPrincipal principal) {

        validateCommunityAccess(principal);

        User user = getUser(principal);

        Post post = Post.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .build();

        postRepository.save(post);

        return post.getId();
    }

    // 댓글 작성
    @Transactional
    public Long createComment(Long postId, CommentCreateRequest request, CustomPrincipal principal) {

        validateCommunityAccess(principal);

        User user = getUser(principal);

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

    // 댓글 조회 (ROLE_GUEST도 가능 여부 정책에 따라)
    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(Long postId, Pageable pageable) {

        return commentRepository
                .findByPostIdAndIsDeletedFalse(postId, pageable)
                .map(CommentResponse::from);
    }

    // 게시글 좋아요
    @Transactional
    public void togglePostLike(Long postId, CustomPrincipal principal) {

        validateCommunityAccess(principal);

        User user = getUser(principal);

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

        validateCommunityAccess(principal);

        User user = getUser(principal);

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

        validateCommunityAccess(principal);

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.INVALID_INPUT_VALUE, "게시글을 찾을 수 없습니다.")
                );

        post.update(request.getTitle(), request.getContent());
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, CustomPrincipal principal) {

        validateCommunityAccess(principal);

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.INVALID_INPUT_VALUE, "게시글을 찾을 수 없습니다.")
                );

        post.softDelete();
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, CustomPrincipal principal) {

        validateCommunityAccess(principal);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.INVALID_INPUT_VALUE, "댓글을 찾을 수 없습니다.")
                );

        comment.softDelete();

        Post post = comment.getPost();
        post.decreaseCommentCount();
    }
}