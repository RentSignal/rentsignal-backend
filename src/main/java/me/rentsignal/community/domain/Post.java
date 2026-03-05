package me.rentsignal.community.domain;

import jakarta.persistence.*;
import lombok.*;
import me.rentsignal.global.entity.BaseTimeEntity;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = false") //  soft delete 자동 필터
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String category;

    private Long userId;

    // neighborhoodId 아직 구현 전

    @Builder.Default
    private Integer likeCount = 0;

    @Builder.Default
    private Integer viewCount = 0;

    @Builder.Default
    private Integer commentCount = 0;

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    public void increaseViewCount() {
        this.viewCount = (this.viewCount == null) ? 1 : this.viewCount + 1;
    }

    public void increaseCommentCount() {
        this.commentCount = (this.commentCount == null) ? 1 : this.commentCount + 1;
    }

    public void decreaseCommentCount() {
        if (this.commentCount == null || this.commentCount <= 0) return;
        this.commentCount = this.commentCount - 1;
    }

    public void increaseLikeCount() {
        this.likeCount = (this.likeCount == null) ? 1 : this.likeCount + 1;
    }

    public void decreaseLikeCount() {
        if (this.likeCount == null || this.likeCount <= 0) return;
        this.likeCount = this.likeCount - 1;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void softDelete() {
        this.isDeleted = true;
    }
}