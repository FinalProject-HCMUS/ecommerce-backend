package com.hcmus.ecommerce_backend.message.model.mapper;

import com.hcmus.ecommerce_backend.message.model.dto.request.CreateMessageRequest;
import com.hcmus.ecommerce_backend.message.model.dto.response.MessageResponse;
import com.hcmus.ecommerce_backend.message.model.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    
    @Mapping(target = "conversationId", source = "conversation.id")
    MessageResponse toResponse(Message message);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conversation", ignore = true)
    @Mapping(target = "contentUrl", source = "contentUrl")
    @Mapping(target = "messageType", source = "messageType")
    @Mapping(target = "isRead", constant = "false")
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Message toEntity(CreateMessageRequest request);
}