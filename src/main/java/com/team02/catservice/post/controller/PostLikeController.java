package com.team02.catservice.post.controller;

import com.team02.catservice.global.response.ApiResponse;
import com.team02.catservice.post.dto.PostResponse;
import com.team02.catservice.post.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/{postId}/likes")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> addLike(@PathVariable Long postId, @AuthenticationPrincipal Long memberId){
        PostResponse response = this.postLikeService.addLike(postId, memberId);
        return ApiResponse.created("좋아요를 눌렀습니다.", response);
    }

    @DeleteMapping
    public ApiResponse<PostResponse> cancelLike(@PathVariable Long postId, @AuthenticationPrincipal Long memberId){
        PostResponse response = this.postLikeService.cancelLike(postId, memberId);
        return ApiResponse.ok("좋아요를 취소했습니다.", response);
    }

}
