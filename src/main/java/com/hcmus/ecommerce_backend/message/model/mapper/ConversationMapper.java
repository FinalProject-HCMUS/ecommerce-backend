package com.hcmus.ecommerce_backend.message.model.mapper;

import com.hcmus.ecommerce_backend.message.model.dto.request.CreateConversationRequest;
import com.hcmus.ecommerce_backend.message.model.dto.request.UpdateConversationRequest;
import com.hcmus.ecommerce_backend.message.model.dto.response.ConversationResponse;
import com.hcmus.ecommerce_backend.message.model.dto.response.MessageResponse;
import com.hcmus.ecommerce_backend.message.model.entity.Conversation;
import com.hcmus.ecommerce_backend.message.model.entity.Message;
import com.hcmus.ecommerce_backend.user.model.dto.response.UserResponse;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.model.mapper.UserMapper;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    @Mapping(target = "latestMessage", source = "messages", qualifiedByName = "findLatestMessage")
    ConversationResponse toResponse(Conversation conversation);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "messages", ignore = true)
    @Mapping(target = "isAdminRead", constant = "false")
    @Mapping(target = "isCustomerRead", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Conversation toEntity(CreateConversationRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "messages", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(UpdateConversationRequest request, @MappingTarget Conversation conversation);

    @Named("findLatestMessage")
    default MessageResponse findLatestMessage(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }

        Message latestMessage = messages.stream()
                .max(Comparator.comparing(Message::getCreatedAt))
                .orElse(null);

        return messageToMessageResponse(latestMessage);
    }

    
    MessageResponse messageToMessageResponse(Message message);
}