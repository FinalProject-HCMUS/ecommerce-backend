package com.hcmus.ecommerce_backend.blog.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import com.hcmus.ecommerce_backend.blog.exception.BlogAlreadyExistsException;
import com.hcmus.ecommerce_backend.blog.exception.BlogNotFoundException;
import com.hcmus.ecommerce_backend.blog.model.dto.request.CreateBlogRequest;
import com.hcmus.ecommerce_backend.blog.model.dto.request.UpdateBlogRequest;
import com.hcmus.ecommerce_backend.blog.model.dto.response.BlogResponse;
import com.hcmus.ecommerce_backend.blog.model.entity.Blog;
import com.hcmus.ecommerce_backend.blog.model.mapper.BlogMapper;
import com.hcmus.ecommerce_backend.blog.repository.BlogRepository;
import com.hcmus.ecommerce_backend.blog.service.BlogService;
import com.hcmus.ecommerce_backend.user.exception.UserNotFoundException;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final BlogMapper blogMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BlogResponse> getAllBlogs(Pageable pageable, String keysearch) {
        log.info("BlogServiceImpl | getAllBlogs | Retrieving blogs with pagination - Page: {}, Size: {}, Sort: {}, KeySearch: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(), keysearch);
        try {
            Page<Blog> blogPage;

            if (keysearch != null && !keysearch.trim().isEmpty()) {
                blogPage = blogRepository.findAll((root, query, criteriaBuilder) -> {
                    return criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + keysearch.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), "%" + keysearch.toLowerCase() + "%")
                    );
                }, pageable);
            } else {
                blogPage = blogRepository.findAll(pageable);
            }

            Page<BlogResponse> blogResponsePage = blogPage.map(blogMapper::toResponse);
            log.info("BlogServiceImpl | getAllBlogs | Found {} blogs on page {} of {}",
                    blogResponsePage.getNumberOfElements(),
                    blogResponsePage.getNumber() + 1,
                    blogResponsePage.getTotalPages());
            return blogResponsePage;
        } catch (DataAccessException e) {
            log.error("BlogServiceImpl | getAllBlogs | Database error retrieving paginated blogs: {}", e.getMessage(), e);
            return Page.empty(pageable);
        } catch (Exception e) {
            log.error("BlogServiceImpl | getAllBlogs | Unexpected error retrieving paginated blogs: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public BlogResponse getBlogById(String id) {
        log.info("BlogServiceImpl | getBlogById | id: {}", id);
        try {
            Blog blog = findBlogById(id);
            log.info("BlogServiceImpl | getBlogById | Blog found: {}", blog.getTitle());
            return blogMapper.toResponse(blog);
        } catch (BlogNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("BlogServiceImpl | getBlogById | Database error for id {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("BlogServiceImpl | getBlogById | Unexpected error for id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public BlogResponse createBlog(CreateBlogRequest request) {
        log.info("BlogServiceImpl | createBlog | Creating blog with title: {}", request.getTitle());
        try {
            // check if blog with the title and user id exists
            checkTitleBlogAndUserIdExists(request.getTitle(), request.getUserId());

            // validate that the user exists
            validateUser(request.getUserId());

            Blog blog = blogMapper.toEntity(request);
            Blog savedBlog = blogRepository.save(blog);
            log.info("BlogServiceImpl | createBlog | Created blog with id: {}", savedBlog.getId());
            return blogMapper.toResponse(savedBlog);
        } catch (BlogAlreadyExistsException | UserNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("BlogServiceImpl | createBlog | Database error creating blog '{}': {}", request.getTitle(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("BlogServiceImpl | createBlog | Unexpected error creating blog '{}': {}", request.getTitle(), e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public BlogResponse updateBlog(String id, UpdateBlogRequest request) {
        log.info("BlogServiceImpl | updateBlog | Updating blog with id: {}", id);
        try {
            Blog blog = findBlogById(id);

            // check if blog with the title and user id exists
            if (!blog.getTitle().equals(request.getTitle())) {
                checkTitleBlogAndUserIdExists(request.getTitle(), blog.getUserId());
            }

            blogMapper.updateEntity(request, blog);
            Blog updatedBlog = blogRepository.save(blog);
            log.info("BlogServiceImpl | updateBlog | Updated blog with id: {}", updatedBlog.getId());
            return blogMapper.toResponse(updatedBlog);
        } catch (BlogNotFoundException | BlogAlreadyExistsException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("BlogServiceImpl | updateBlog | Database error updating blog with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("BlogServiceImpl | updateBlog | Unexpected error updating blog with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void deleteBlog(String id) {
        log.info("BlogServiceImpl | deleteBlog | Deleting blog with id: {}", id);
        try {
            if (!blogRepository.existsById(id)) {
                throw new BlogNotFoundException(id);
            }
            blogRepository.deleteById(id);
            log.info("BlogServiceImpl | deleteBlog | Deleted blog with id: {}", id);
        } catch (BlogNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("BlogServiceImpl | deleteBlog | Database error deleting blog with id '{}': {}",
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("BlogServiceImpl | deleteBlog | Unexpected error deleting blog with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Blog findBlogById(String id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("BlogServiceImpl | findBlogById | Blog not found with id: {}", id);
                    return new BlogNotFoundException(id);
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    protected boolean doesProductExistById(String id) {
        return userRepository.existsById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void checkTitleBlogAndUserIdExists(String title, String userId) {
        if (blogRepository.existsByTitleAndUserId(title, userId)) {
            log.error("BlogServiceImpl | checkTitleBlogAndUserIdExists | Blog already exists with title: {} and user id: {}", title, userId);
            throw new BlogAlreadyExistsException(title, userId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void validateUser(String userId) {
        if (!userRepository.existsById(userId)) {
            log.error("BlogServiceImpl | validateUser | User not found with id: {}", userId);
            throw new UserNotFoundException(userId);
        }
    }
}
