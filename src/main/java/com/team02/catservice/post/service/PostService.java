package com.team02.catservice.post.service;

import com.team02.catservice.cat.entity.Cat;
import com.team02.catservice.cat.service.CatService;
import com.team02.catservice.member.entity.Member;
import com.team02.catservice.member.service.MemberService;
import com.team02.catservice.post.dto.PostCreateRequest;
import com.team02.catservice.post.dto.PostResponse;
import com.team02.catservice.post.entity.Post;
import com.team02.catservice.post.repository.PostRepository;
import com.team02.catservice.tag.entity.Tag;
import com.team02.catservice.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberService memberService;
    private final CatService catService;
    private final TagService tagService;


    @Transactional
    public PostResponse create(Long memberId,PostCreateRequest request){
        Member member = this.memberService.getMember(memberId);

        Cat cat = request.catId() == null ? null : this.catService.getCat(request.catId());

        Post post = new Post(request.title(), request.content(), request.category(), request.imageUrl(),
                member, cat);

        List<Tag> tags = this.tagService.findOrCreateAll(request.tags());
        for (Tag tag : tags) {
            post.addTag(tag);
        }

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

