package com.team02.catservice.post.listener;

import com.team02.catservice.cat.event.CatDeletedEvent;
import com.team02.catservice.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostEventListener {

    private final PostService postService;

    @EventListener
    public void handleCatDeleted(CatDeletedEvent event){
        this.postService.releaseCatFromPosts(event.catId());
    }

}
