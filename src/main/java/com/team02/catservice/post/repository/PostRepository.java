package com.team02.catservice.post.repository;

import com.team02.catservice.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByCatId(Long catId);
}
