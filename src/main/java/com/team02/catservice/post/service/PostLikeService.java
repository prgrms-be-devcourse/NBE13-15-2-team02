package com.team02.catservice.post.service;

import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;
import com.team02.catservice.member.entity.Member;
import com.team02.catservice.member.service.MemberService;
import com.team02.catservice.post.dto.PostResponse;
import com.team02.catservice.post.entity.Post;
import com.team02.catservice.post.entity.PostLike;
import com.team02.catservice.post.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostService postService;
    private final MemberService memberService;

    @Transactional
    public PostResponse addLike(Long postId, Long memberId){
        Post post = postService.getPost(postId);

        if (postLikeRepository.existsByPostIdAndMemberId(postId, memberId)){
            throw new BusinessException(ErrorCode.ALREADY_LIKED);
        }

        Member member = memberService.getMember(memberId);

        PostLike postLike = new PostLike(post, member);

        postLikeRepository.save(postLike);

        post.increaseLikeCount();

        return PostResponse.from(post);
    }

    @Transactional
    public PostResponse cancelLike(Long postId, Long memberId){
        Post post = postService.getPost(postId);

        PostLike postLike = postLikeRepository.findByPostIdAndMemberId(postId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LIKE_NOT_FOUND));

        postLikeRepository.delete(postLike);

        post.decreaseLikeCount();

        return PostResponse.from(post);
    }

}
