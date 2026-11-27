package com.team02.catservice.post.service;

import com.team02.catservice.cat.entity.Cat;
import com.team02.catservice.cat.service.CatService;
import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;
import com.team02.catservice.global.exception.GlobalExceptionHandler;
import com.team02.catservice.member.entity.Member;
import com.team02.catservice.member.service.MemberService;
import com.team02.catservice.post.dto.PostCreateRequest;
import com.team02.catservice.post.dto.PostResponse;
import com.team02.catservice.post.dto.PostUpdateRequest;
import com.team02.catservice.post.entity.Post;
import com.team02.catservice.post.event.PostDeleteEvent;
import com.team02.catservice.post.repository.PostLikeRepository;
import com.team02.catservice.post.repository.PostRepository;
import com.team02.catservice.tag.entity.Tag;
import com.team02.catservice.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final PostLikeRepository postLikeRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    public PostResponse create(Long memberId,PostCreateRequest request) {
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

    @Transactional(readOnly = true)
    public Post getPost(Long postId){
        return postRepository.findById(postId).orElseThrow(()-> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PostResponse getPostDetail(Long postId){
        Post post = getPost(postId);
        return PostResponse.from(post);
    }

    @Transactional
    public PostResponse updatePost(Long postId, Long memberId, PostUpdateRequest request){
        Post post = getPost(postId);

        post.validateWriter(memberId);

        post.update(request.title(), request.content(), request.category(), request.imageUrl());

        if (request.tags() != null){
            List<Tag> tags = this.tagService.findOrCreateAll(request.tags());
            post.replaceTags(tags);
        }

        return PostResponse.from(post);
    }

    @Transactional
    public void releaseCatFromPosts(Long catId){
        List<Post> posts = this.postRepository.findByCatId(catId);
        for (Post post: posts){
            post.releaseCat();
        }
    }

    @Transactional
    public void deletePost(Long memberId, Long postId) {
        Post post = getPost(postId);

        post.validateWriter(memberId);

        eventPublisher.publishEvent(new PostDeleteEvent(postId));

        postLikeRepository.deleteByPostId(postId);

        postRepository.delete(post);
    }


}

