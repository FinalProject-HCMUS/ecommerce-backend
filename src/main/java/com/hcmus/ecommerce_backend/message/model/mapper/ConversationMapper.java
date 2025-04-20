package com.hcmus.ecommerce_backend.message.model.mapper;

import com.hcmus.ecommerce_backend.message.model.dto.request.CreateConversationRequest;
import com.hcmus.ecommerce_backend.message.model.dto.request.UpdateConversationRequest;
import com.hcmus.ecommerce_backend.message.model.dto.response.ConversationResponse;
import com.hcmus.ecommerce_backend.message.model.dto.response.MessageResponse;
import com.hcmus.ecommerce_backend.message.model.entity.Conversation;
import com.hcmus.ecommerce_backend.message.model.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ConversationMapper {
    
    @Mapping(target = "messages", source = "messages", qualifiedByName = "messagesToMessageResponses")
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
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "messages", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(UpdateConversationRequest request, @MappingTarget Conversation conversation);
    
    @Named("messagesToMessageResponses")
    default List<MessageResponse> messagesToMessageResponses(List<Message> messages) {
        if (messages == null) {
            return null;
        }
        
        return messages.stream()
                .map(this::messageToMessageResponse)
                .collect(Collectors.toList());
    }
    
    MessageResponse messageToMessageResponse(Message message);
}