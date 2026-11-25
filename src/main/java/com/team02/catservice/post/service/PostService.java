package com.team02.catservice.post.service;

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


    @Transactional
    public void releaseCatFromPosts(Long catId){
        List<Post> posts = this.postRepository.findByCatId(catId);
        for (Post post: posts){
            post.releaseCat();
        }
    }

}

