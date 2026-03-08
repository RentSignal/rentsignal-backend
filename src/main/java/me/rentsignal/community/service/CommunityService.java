package me.rentsignal.community.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.community.domain.*;
import me.rentsignal.community.dto.*;
import me.rentsignal.community.repository.*;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
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

    // 게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<PostListItemResponse> getPosts(String category, String keyword, Pageable pageable) {

        return postRepository.search(category, keyword, pageable)
                .map(PostListItemResponse::from);
    }

    // 게시글 상세 조회
    @Transactional
    public PostDetailResponse getPostDetail(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.INVALID_INPUT_VALUE, "게시글을 찾을 수 없습니다.")
                );

        if (post.getIsDeleted()) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "삭제된 게시글입니다.");
        }

        post.increaseViewCount();

        return PostDetailResponse.from(post);
    }

    // 게시글 작성
    @Transactional
    public Long createPost(Long userId, PostCreateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

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
    public Long createComment(Long userId, Long postId, CommentCreateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

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
    public Page<CommentResponse> getComments(Long postId, Pageable pageable) {

        return commentRepository
                .findByPostIdAndIsDeletedFalse(postId, pageable)
                .map(CommentResponse::from);
    }

    // 게시글 좋아요
    @Transactional
    public void togglePostLike(Long userId, Long postId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

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
    public void toggleCommentLike(Long userId, Long commentId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

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
    public void updatePost(Long postId, PostUpdateRequest request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.INVALID_INPUT_VALUE, "게시글을 찾을 수 없습니다.")
                );

        post.update(request.getTitle(), request.getContent());
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.INVALID_INPUT_VALUE, "게시글을 찾을 수 없습니다.")
                );

        post.softDelete();
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.INVALID_INPUT_VALUE, "댓글을 찾을 수 없습니다.")
                );

        comment.softDelete();

        Post post = comment.getPost();
        post.decreaseCommentCount();
    }
}