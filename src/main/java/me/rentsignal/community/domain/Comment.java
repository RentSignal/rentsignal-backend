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
@Where(clause = "is_deleted = false") // ✅ soft delete
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;

    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    private Integer likeCount = 0;

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    public void increaseLikeCount() {
        this.likeCount = (this.likeCount == null) ? 1 : this.likeCount + 1;
    }

    public void decreaseLikeCount() {
        if (this.likeCount == null || this.likeCount <= 0) return;
        this.likeCount = this.likeCount - 1;
    }

    public void softDelete() {
        this.isDeleted = true;
    }
}