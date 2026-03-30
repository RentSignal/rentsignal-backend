package me.rentsignal.community.domain;

import jakarta.persistence.*;
import lombok.*;
import me.rentsignal.global.entity.BaseTimeEntity;
import me.rentsignal.user.entity.User;
import org.hibernate.annotations.Where;
import me.rentsignal.location.entity.Neighborhood;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = false")
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighborhood_id")
    private Neighborhood neighborhood;

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
        this.viewCount++;
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) this.commentCount--;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateCategory(Category category) {
        this.category = category;
    }

    public void softDelete() {
        this.isDeleted = true;
    }
}