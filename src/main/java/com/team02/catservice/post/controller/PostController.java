package com.team02.catservice.post.controller;

import com.team02.catservice.global.response.ApiResponse;
import com.team02.catservice.post.dto.PostCreateRequest;
import com.team02.catservice.post.dto.PostResponse;
import com.team02.catservice.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> create(@AuthenticationPrincipal Long memberId, @Valid @RequestBody PostCreateRequest request){
        PostResponse response = this.postService.create(memberId, request);
        return ApiResponse.created("게시글을 작성했습니다.", response);
    }

}
