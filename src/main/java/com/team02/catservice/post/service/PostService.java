package com.team02.catservice.post.service;

import com.team02.catservice.member.entity.Member;
import com.team02.catservice.member.repository.MemberRepository;
import com.team02.catservice.member.service.MemberService;
import com.team02.catservice.post.dto.PostCreateRequest;
import com.team02.catservice.post.dto.PostResponse;
import com.team02.catservice.post.entity.Post;
import com.team02.catservice.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberService memberService;


    @Transactional
    public PostResponse create(Long memberId, PostCreateRequest request){
        Member member = this.memberService.getMember(memberId);

        //TODO cat 조회 로직

        Post post = new Post(request.title(), request.content(), request.category(), request.imageUrl(),
                member, null); //TODO cat 조회 로직의 변수로 변경.

        //TODO tag 붙이는 로직

        postRepository.save(post);

        return PostResponse.from(post);
    }




    @Transactional
    public void releaseCatFromPosts(Long catId){
        List<Post> posts = this.postRepository.findByCatId(catId);
        for (Post post: posts){
            post.releaseCat();
        }
    }

}

