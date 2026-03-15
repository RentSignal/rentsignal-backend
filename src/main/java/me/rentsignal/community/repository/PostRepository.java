package me.rentsignal.community.repository;

import me.rentsignal.community.domain.Post;
import me.rentsignal.user.entity.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {


    @Query("""
select p from Post p
where p.isDeleted = false
  and (:category is null or p.category = :category)
  and (:neighborhoodId is null or p.neighborhood.id = :neighborhoodId)
order by p.createdAt desc
""")

    Page<Post> search(
            @Param("category") String category,
            @Param("neighborhoodId") Long neighborhoodId,
            Pageable pageable
    );

    // 내가 쓴 글 조회
    Page<Post> findByUser(User user, Pageable pageable);
}
