package com.team02.catservice.post.entity;

import com.team02.catservice.cat.entity.Cat;
import com.team02.catservice.global.entity.BaseEntity;
import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;
import com.team02.catservice.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cat_id", nullable = true)
    private Cat cat;


    public Post(String title, String content, PostCategory category, String imageUrl, Member member, Cat cat) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.imageUrl = imageUrl;
        this.member = member;
        this.cat = cat;
        validateCatRequired();
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
            validateCatRequired();
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

    //작성 시: 사용자가 catId 없이 입양 글을 쓰려고 함 → 생성자 끝에서 검사에 걸려 400 에러
    //수정 시: 고양이 없는 일상(DAILY) 글의 카테고리를 입양으로 바꾸려 함 → update에서 검사에 걸려 400 에러
    private void validateCatRequired(){
        if ((this.category == PostCategory.LOST ||
                this.category == PostCategory.ADOPTION)
                && this.cat == null) {
            throw new BusinessException(ErrorCode.CAT_REQUIRED);
        }
    }

    // 고양이 삭제 이벤트 처리용, 입양·분실 글이어도 cat 필수 검사를 하지 않음
    public void releaseCat(){
        this.cat = null;
    }

}
