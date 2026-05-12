package me.rentsignal.home.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.community.dto.PostListItemResponse;
import me.rentsignal.community.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final PostRepository postRepository;

    public List<PostListItemResponse> getHomeReviews() {
        return postRepository.findTop5ByIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(PostListItemResponse::from)
                .toList();
    }
}
