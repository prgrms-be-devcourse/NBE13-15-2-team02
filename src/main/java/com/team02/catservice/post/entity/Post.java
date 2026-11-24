package com.team02.catservice.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING) // 문자열로 저장하기 위함.
    @Column(nullable = false, length = 30)
    private PostCategory category;

    @Column(nullable = true)
    private String imageUrl;

    @Column(nullable = false)
    private int likeCount;

    //@ManyToOne(fetch = FetchType.LAZY, optional = false)
    //@JoinColumn(name = "member_id", nullable = false)
    // private Member member; # Member 엔티티 merge 후 주석 해제

    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "cat_id", nullable = true)
    //private Cat cat; # Cat 엔티티 merge 후 주석 해제


    public Post(String title, String content, PostCategory category, String imageUrl) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.imageUrl = imageUrl;
        //Member와 Cat도 추가해야 함.
    }

    public void update(String title, String content, PostCategory category, String imageUrl){
        if (title != null){
            this.title = title;
        }
        if (content != null){
            this.content = content;
        }
        if (category != null){
            this.category = category;
        }
        if (imageUrl != null){
            this.imageUrl = imageUrl;
        }
    }

    public void increaseLikeCount(){
        likeCount++;
    }

    public void decreaseLikeCount(){
        if (likeCount <= 0){
            throw new IllegalStateException("좋아요 수는 0보다 작아질 수 없습니다");
        }
        likeCount--;
    }

}
