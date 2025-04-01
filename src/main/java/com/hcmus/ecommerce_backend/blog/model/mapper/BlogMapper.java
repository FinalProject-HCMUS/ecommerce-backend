package com.hcmus.ecommerce_backend.blog.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.hcmus.ecommerce_backend.blog.model.dto.request.CreateBlogRequest;
import com.hcmus.ecommerce_backend.blog.model.dto.request.UpdateBlogRequest;
import com.hcmus.ecommerce_backend.blog.model.dto.response.BlogResponse;
import com.hcmus.ecommerce_backend.blog.model.entity.Blog;
import com.hcmus.ecommerce_backend.user.model.entity.User;

@Mapper(componentModel = "spring")
public interface BlogMapper {
    
    @Mapping(target="userId", source="user", qualifiedByName="userToUserId")
    BlogResponse toResponse(Blog blog);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target="user", source="userId", qualifiedByName="userIdToUser")
    Blog toEntity(CreateBlogRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target="user", source="userId", qualifiedByName="userIdToUser")
    void updateEntity(UpdateBlogRequest blogReponse, @MappingTarget Blog blog);

    @Named("userToUserId")
    default String userToUserId(User user) {
        if (user == null) {
            return null;
        }
        return user.getId();
    }

    @Named("userIdToUser")
    default User userIdToUser(String id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }
}
