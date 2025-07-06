package com.hcmus.ecommerce_backend.unit.order.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import com.hcmus.ecommerce_backend.order.exception.OrderNotFoundException;
import com.hcmus.ecommerce_backend.order.exception.OrderTrackNotFoundException;
import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderTrackRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderTrackRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderTrackResponse;
import com.hcmus.ecommerce_backend.order.model.entity.Order;
import com.hcmus.ecommerce_backend.order.model.entity.OrderTrack;
import com.hcmus.ecommerce_backend.order.model.enums.Status;
import com.hcmus.ecommerce_backend.order.model.mapper.OrderTrackMapper;
import com.hcmus.ecommerce_backend.order.repository.OrderRepository;
import com.hcmus.ecommerce_backend.order.repository.OrderTrackRepository;
import com.hcmus.ecommerce_backend.order.service.impl.OrderTrackServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderTrackServiceImplTest {

    @Mock
    private OrderTrackRepository orderTrackRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderTrackMapper orderTrackMapper;

    @InjectMocks
    private OrderTrackServiceImpl orderTrackService;

    private OrderTrack orderTrack;
    private OrderTrackResponse orderTrackResponse;
    private CreateOrderTrackRequest createRequest;
    private UpdateOrderTrackRequest updateRequest;
    private Order order;

    @BeforeEach
    void setUp() {
        // Setup Order
        order = new Order();
        order.setId("order-1");
        order.setStatus(Status.NEW);
        order.setCustomerId("customer-1");

        // Setup OrderTrack
        orderTrack = new OrderTrack();
        orderTrack.setId("track-1");
        orderTrack.setOrder(order);
        orderTrack.setStatus(Status.NEW);
        orderTrack.setNotes("Order placed");
        orderTrack.setCreatedAt(LocalDateTime.now());
        orderTrack.setUpdatedAt(LocalDateTime.now());

        // Setup OrderTrackResponse
        orderTrackResponse = OrderTrackResponse.builder()
                .id("track-1")
                .status(Status.NEW)
                .notes("Order placed")
                .build();

        // Setup CreateOrderTrackRequest
        createRequest = CreateOrderTrackRequest.builder()
                .orderId("order-1")
                .status(Status.NEW)
                .notes("Order placed")
                .build();

        // Setup UpdateOrderTrackRequest
        updateRequest = UpdateOrderTrackRequest.builder()
                .status(Status.PROCESSING)
                .notes("Order is being processed")
                .build();
    }

    @Test
    void getAllOrderTracks_Success() {
        // Given
        List<OrderTrack> orderTracks = Arrays.asList(orderTrack);
        when(orderTrackRepository.findAll()).thenReturn(orderTracks);
        when(orderTrackMapper.toResponse(orderTrack)).thenReturn(orderTrackResponse);

        // When
        List<OrderTrackResponse> result = orderTrackService.getAllOrderTracks();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(orderTrackResponse, result.get(0));
        verify(orderTrackRepository).findAll();
        verify(orderTrackMapper).toResponse(orderTrack);
    }

    @Test
    void getAllOrderTracks_EmptyList() {
        // Given
        when(orderTrackRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<OrderTrackResponse> result = orderTrackService.getAllOrderTracks();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderTrackRepository).findAll();
    }

    @Test
    void getAllOrderTracks_DatabaseError() {
        // Given
        when(orderTrackRepository.findAll()).thenThrow(new DataIntegrityViolationException("Database error"));

        // When
        List<OrderTrackResponse> result = orderTrackService.getAllOrderTracks();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderTrackRepository).findAll();
    }

    @Test
    void getAllOrderTracks_UnexpectedError() {
        // Given
        when(orderTrackRepository.findAll()).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> orderTrackService.getAllOrderTracks());
        verify(orderTrackRepository).findAll();
    }

    @Test
    void getOrderTrackById_Success() {
        // Given
        String id = "track-1";
        when(orderTrackRepository.findById(id)).thenReturn(Optional.of(orderTrack));
        when(orderTrackMapper.toResponse(orderTrack)).thenReturn(orderTrackResponse);

        // When
        OrderTrackResponse result = orderTrackService.getOrderTrackById(id);

        // Then
        assertNotNull(result);
        assertEquals(orderTrackResponse, result);
        verify(orderTrackRepository).findById(id);
        verify(orderTrackMapper).toResponse(orderTrack);
    }

    @Test
    void getOrderTrackById_NotFound() {
        // Given
        String id = "non-existent";
        when(orderTrackRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderTrackNotFoundException.class, () -> orderTrackService.getOrderTrackById(id));
        verify(orderTrackRepository).findById(id);
    }

    @Test
    void getOrderTrackById_DatabaseError() {
        // Given
        String id = "track-1";
        when(orderTrackRepository.findById(id)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> orderTrackService.getOrderTrackById(id));
        verify(orderTrackRepository).findById(id);
    }

    @Test
    void getOrderTrackById_UnexpectedError() {
        // Given
        String id = "track-1";
        when(orderTrackRepository.findById(id)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> orderTrackService.getOrderTrackById(id));
        verify(orderTrackRepository).findById(id);
    }

    @Test
    void getOrderTracksByOrderId_Success() {
        // Given
        String orderId = "order-1";
        List<OrderTrack> orderTracks = Arrays.asList(orderTrack);
        when(orderTrackRepository.findByOrderIdOrderByUpdatedAtDesc(orderId)).thenReturn(orderTracks);
        when(orderTrackMapper.toResponse(orderTrack)).thenReturn(orderTrackResponse);

        // When
        List<OrderTrackResponse> result = orderTrackService.getOrderTracksByOrderId(orderId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(orderTrackResponse, result.get(0));
        verify(orderTrackRepository).findByOrderIdOrderByUpdatedAtDesc(orderId);
        verify(orderTrackMapper).toResponse(orderTrack);
    }

    @Test
    void getOrderTracksByOrderId_EmptyList() {
        // Given
        String orderId = "order-1";
        when(orderTrackRepository.findByOrderIdOrderByUpdatedAtDesc(orderId)).thenReturn(Collections.emptyList());

        // When
        List<OrderTrackResponse> result = orderTrackService.getOrderTracksByOrderId(orderId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderTrackRepository).findByOrderIdOrderByUpdatedAtDesc(orderId);
    }

    @Test
    void getOrderTracksByOrderId_DatabaseError() {
        // Given
        String orderId = "order-1";
        when(orderTrackRepository.findByOrderIdOrderByUpdatedAtDesc(orderId))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When
        List<OrderTrackResponse> result = orderTrackService.getOrderTracksByOrderId(orderId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderTrackRepository).findByOrderIdOrderByUpdatedAtDesc(orderId);
    }

    @Test
    void getOrderTracksByOrderId_UnexpectedError() {
        // Given
        String orderId = "order-1";
        when(orderTrackRepository.findByOrderIdOrderByUpdatedAtDesc(orderId))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> orderTrackService.getOrderTracksByOrderId(orderId));
        verify(orderTrackRepository).findByOrderIdOrderByUpdatedAtDesc(orderId);
    }

    @Test
    void getOrderTracksByStatus_Success() {
        // Given
        Status status = Status.NEW;
        List<OrderTrack> orderTracks = Arrays.asList(orderTrack);
        when(orderTrackRepository.findByStatusOrderByUpdatedAtDesc(status)).thenReturn(orderTracks);
        when(orderTrackMapper.toResponse(orderTrack)).thenReturn(orderTrackResponse);

        // When
        List<OrderTrackResponse> result = orderTrackService.getOrderTracksByStatus(status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(orderTrackResponse, result.get(0));
        verify(orderTrackRepository).findByStatusOrderByUpdatedAtDesc(status);
        verify(orderTrackMapper).toResponse(orderTrack);
    }

    @Test
    void getOrderTracksByStatus_EmptyList() {
        // Given
        Status status = Status.NEW;
        when(orderTrackRepository.findByStatusOrderByUpdatedAtDesc(status)).thenReturn(Collections.emptyList());

        // When
        List<OrderTrackResponse> result = orderTrackService.getOrderTracksByStatus(status);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderTrackRepository).findByStatusOrderByUpdatedAtDesc(status);
    }

    @Test
    void getOrderTracksByStatus_DatabaseError() {
        // Given
        Status status = Status.NEW;
        when(orderTrackRepository.findByStatusOrderByUpdatedAtDesc(status))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When
        List<OrderTrackResponse> result = orderTrackService.getOrderTracksByStatus(status);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderTrackRepository).findByStatusOrderByUpdatedAtDesc(status);
    }

    @Test
    void getOrderTracksByStatus_UnexpectedError() {
        // Given
        Status status = Status.NEW;
        when(orderTrackRepository.findByStatusOrderByUpdatedAtDesc(status))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> orderTrackService.getOrderTracksByStatus(status));
        verify(orderTrackRepository).findByStatusOrderByUpdatedAtDesc(status);
    }

    @Test
    void createOrderTrack_Success() {
        // Given
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderTrackMapper.toEntity(createRequest)).thenReturn(orderTrack);
        when(orderTrackRepository.save(orderTrack)).thenReturn(orderTrack);
        when(orderTrackMapper.toResponse(orderTrack)).thenReturn(orderTrackResponse);

        // When
        OrderTrackResponse result = orderTrackService.createOrderTrack(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(orderTrackResponse, result);
        verify(orderRepository).findById("order-1");
        verify(orderTrackMapper).toEntity(createRequest);
        verify(orderTrackRepository).save(orderTrack);
        verify(orderTrackMapper).toResponse(orderTrack);
    }

    @Test
    void createOrderTrack_OrderNotFound() {
        // Given
        when(orderRepository.findById("order-1")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderNotFoundException.class, () -> orderTrackService.createOrderTrack(createRequest));
        verify(orderRepository).findById("order-1");
        verify(orderTrackRepository, never()).save(any());
    }

    @Test
    void createOrderTrack_UpdateOrderStatus() {
        // Given
        order.setStatus(Status.NEW);
        createRequest.setStatus(Status.PROCESSING);
        
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderTrackMapper.toEntity(createRequest)).thenReturn(orderTrack);
        when(orderTrackRepository.save(orderTrack)).thenReturn(orderTrack);
        when(orderTrackMapper.toResponse(orderTrack)).thenReturn(orderTrackResponse);

        // When
        OrderTrackResponse result = orderTrackService.createOrderTrack(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(Status.PROCESSING, order.getStatus());
        verify(orderRepository).save(order);
        verify(orderTrackRepository).save(orderTrack);
    }

    @Test
    void createOrderTrack_SameStatus_NoOrderUpdate() {
        // Given
        order.setStatus(Status.NEW);
        createRequest.setStatus(Status.NEW);
        
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderTrackMapper.toEntity(createRequest)).thenReturn(orderTrack);
        when(orderTrackRepository.save(orderTrack)).thenReturn(orderTrack);
        when(orderTrackMapper.toResponse(orderTrack)).thenReturn(orderTrackResponse);

        // When
        OrderTrackResponse result = orderTrackService.createOrderTrack(createRequest);

        // Then
        assertNotNull(result);
        verify(orderRepository, never()).save(order);
        verify(orderTrackRepository).save(orderTrack);
    }

    @Test
    void createOrderTrack_DatabaseError() {
        // Given
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderTrackMapper.toEntity(createRequest)).thenReturn(orderTrack);
        when(orderTrackRepository.save(orderTrack)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> orderTrackService.createOrderTrack(createRequest));
        verify(orderRepository).findById("order-1");
        verify(orderTrackRepository).save(orderTrack);
    }

    @Test
    void createOrderTrack_UnexpectedError() {
        // Given
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderTrackMapper.toEntity(createRequest)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> orderTrackService.createOrderTrack(createRequest));
        verify(orderRepository).findById("order-1");
        verify(orderTrackMapper).toEntity(createRequest);
    }

    @Test
    void updateOrderTrack_Success() {
        // Given
        String id = "track-1";
        when(orderTrackRepository.findById(id)).thenReturn(Optional.of(orderTrack));
        when(orderTrackRepository.save(orderTrack)).thenReturn(orderTrack);
        when(orderTrackMapper.toResponse(orderTrack)).thenReturn(orderTrackResponse);

        // When
        OrderTrackResponse result = orderTrackService.updateOrderTrack(id, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(orderTrackResponse, result);
        verify(orderTrackRepository).findById(id);
        verify(orderTrackMapper).updateEntity(updateRequest, orderTrack);
        verify(orderTrackRepository).save(orderTrack);
        verify(orderTrackMapper).toResponse(orderTrack);
    }

    @Test
    void updateOrderTrack_NotFound() {
        // Given
        String id = "non-existent";
        when(orderTrackRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderTrackNotFoundException.class, () -> orderTrackService.updateOrderTrack(id, updateRequest));
        verify(orderTrackRepository).findById(id);
        verify(orderTrackMapper, never()).updateEntity(any(), any());
    }

    @Test
    void updateOrderTrack_WithOrderIdChange() {
        // Given
        String id = "track-1";
        String newOrderId = "order-2";
        updateRequest.setOrderId(newOrderId);
        
        Order newOrder = new Order();
        newOrder.setId(newOrderId);
        
        when(orderTrackRepository.findById(id)).thenReturn(Optional.of(orderTrack));
        when(orderRepository.findById(newOrderId)).thenReturn(Optional.of(newOrder));
        when(orderTrackRepository.save(orderTrack)).thenReturn(orderTrack);
        when(orderTrackMapper.toResponse(orderTrack)).thenReturn(orderTrackResponse);

        // When
        OrderTrackResponse result = orderTrackService.updateOrderTrack(id, updateRequest);

        // Then
        assertNotNull(result);
        verify(orderRepository).findById(newOrderId);
        verify(orderTrackRepository).save(orderTrack);
    }

    @Test
    void updateOrderTrack_OrderIdChange_OrderNotFound() {
        // Given
        String id = "track-1";
        String newOrderId = "non-existent-order";
        updateRequest.setOrderId(newOrderId);
        
        when(orderTrackRepository.findById(id)).thenReturn(Optional.of(orderTrack));
        when(orderRepository.findById(newOrderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderNotFoundException.class, () -> orderTrackService.updateOrderTrack(id, updateRequest));
        verify(orderRepository).findById(newOrderId);
        verify(orderTrackRepository, never()).save(any());
    }

    @Test
    void updateOrderTrack_StatusChange_UpdatesOrder() {
        // Given
        String id = "track-1";
        orderTrack.setStatus(Status.NEW);
        updateRequest.setStatus(Status.PROCESSING);
        
        when(orderTrackRepository.findById(id)).thenReturn(Optional.of(orderTrack));
        when(orderTrackRepository.save(orderTrack)).thenReturn(orderTrack);
        when(orderTrackMapper.toResponse(orderTrack)).thenReturn(orderTrackResponse);

        // When
        OrderTrackResponse result = orderTrackService.updateOrderTrack(id, updateRequest);

        // Then
        assertNotNull(result);
        verify(orderRepository).save(order);
        assertEquals(Status.PROCESSING, order.getStatus());
    }

    @Test
    void updateOrderTrack_SameStatus_NoOrderUpdate() {
        // Given
        String id = "track-1";
        orderTrack.setStatus(Status.NEW);
        updateRequest.setStatus(Status.NEW);
        
        when(orderTrackRepository.findById(id)).thenReturn(Optional.of(orderTrack));
        when(orderTrackRepository.save(orderTrack)).thenReturn(orderTrack);
        when(orderTrackMapper.toResponse(orderTrack)).thenReturn(orderTrackResponse);

        // When
        OrderTrackResponse result = orderTrackService.updateOrderTrack(id, updateRequest);

        // Then
        assertNotNull(result);
        verify(orderRepository, never()).save(order);
    }

    @Test
    void updateOrderTrack_DatabaseError() {
        // Given
        String id = "track-1";
        when(orderTrackRepository.findById(id)).thenReturn(Optional.of(orderTrack));
        when(orderTrackRepository.save(orderTrack)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> orderTrackService.updateOrderTrack(id, updateRequest));
        verify(orderTrackRepository).findById(id);
        verify(orderTrackRepository).save(orderTrack);
    }

    @Test
    void deleteOrderTrack_Success() {
        // Given
        String id = "track-1";
        when(orderTrackRepository.existsById(id)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> orderTrackService.deleteOrderTrack(id));

        // Then
        verify(orderTrackRepository).existsById(id);
        verify(orderTrackRepository).deleteById(id);
    }

    @Test
    void deleteOrderTrack_NotFound() {
        // Given
        String id = "non-existent";
        when(orderTrackRepository.existsById(id)).thenReturn(false);

        // When & Then
        assertThrows(OrderTrackNotFoundException.class, () -> orderTrackService.deleteOrderTrack(id));
        verify(orderTrackRepository).existsById(id);
        verify(orderTrackRepository, never()).deleteById(id);
    }

    @Test
    void deleteOrderTrack_DatabaseError() {
        // Given
        String id = "track-1";
        when(orderTrackRepository.existsById(id)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Database error")).when(orderTrackRepository).deleteById(id);

        // When & Then
        assertThrows(DataAccessException.class, () -> orderTrackService.deleteOrderTrack(id));
        verify(orderTrackRepository).existsById(id);
        verify(orderTrackRepository).deleteById(id);
    }

    @Test
    void deleteOrderTrack_UnexpectedError() {
        // Given
        String id = "track-1";
        when(orderTrackRepository.existsById(id)).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(orderTrackRepository).deleteById(id);

        // When & Then
        assertThrows(RuntimeException.class, () -> orderTrackService.deleteOrderTrack(id));
        verify(orderTrackRepository).existsById(id);
        verify(orderTrackRepository).deleteById(id);
    }

    @Test
    void updateOrderTrack_NullOrderId_NoOrderValidation() {
        // Given
        String id = "track-1";
        updateRequest.setOrderId(null);
        
        when(orderTrackRepository.findById(id)).thenReturn(Optional.of(orderTrack));
        when(orderTrackRepository.save(orderTrack)).thenReturn(orderTrack);
        when(orderTrackMapper.toResponse(orderTrack)).thenReturn(orderTrackResponse);

        // When
        OrderTrackResponse result = orderTrackService.updateOrderTrack(id, updateRequest);

        // Then
        assertNotNull(result);
        verify(orderRepository, never()).findById(anyString());
        verify(orderTrackRepository).save(orderTrack);
    }

    @Test
    void updateOrderTrack_SameOrderId_NoOrderValidation() {
        // Given
        String id = "track-1";
        updateRequest.setOrderId("order-1"); // Same as existing order ID
        
        when(orderTrackRepository.findById(id)).thenReturn(Optional.of(orderTrack));
        when(orderTrackRepository.save(orderTrack)).thenReturn(orderTrack);
        when(orderTrackMapper.toResponse(orderTrack)).thenReturn(orderTrackResponse);

        // When
        OrderTrackResponse result = orderTrackService.updateOrderTrack(id, updateRequest);

        // Then
        assertNotNull(result);
        verify(orderRepository, never()).findById("order-1");
        verify(orderTrackRepository).save(orderTrack);
    }

    @Test
    void updateOrderTrack_NullStatus_NoOrderStatusUpdate() {
        // Given
        String id = "track-1";
        updateRequest.setStatus(null);
        
        when(orderTrackRepository.findById(id)).thenReturn(Optional.of(orderTrack));
        when(orderTrackRepository.save(orderTrack)).thenReturn(orderTrack);
        when(orderTrackMapper.toResponse(orderTrack)).thenReturn(orderTrackResponse);

        // When
        OrderTrackResponse result = orderTrackService.updateOrderTrack(id, updateRequest);

        // Then
        assertNotNull(result);
        verify(orderRepository, never()).save(order);
        verify(orderTrackRepository).save(orderTrack);
    }

    @Test
    void createOrderTrack_MultipleOrderTracks() {
        // Given
        OrderTrack track1 = new OrderTrack();
        track1.setId("track-1");
        track1.setOrder(order);
        track1.setStatus(Status.NEW);

        OrderTrack track2 = new OrderTrack();
        track2.setId("track-2");
        track2.setOrder(order);
        track2.setStatus(Status.PROCESSING);

        OrderTrackResponse response1 = OrderTrackResponse.builder()
                .id("track-1")
                .status(Status.NEW)
                .build();

        OrderTrackResponse response2 = OrderTrackResponse.builder()
                .id("track-2")
                .status(Status.PROCESSING)
                .build();

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderTrackMapper.toEntity(any(CreateOrderTrackRequest.class))).thenReturn(track1, track2);
        when(orderTrackRepository.save(any(OrderTrack.class))).thenReturn(track1, track2);
        when(orderTrackMapper.toResponse(track1)).thenReturn(response1);
        when(orderTrackMapper.toResponse(track2)).thenReturn(response2);

        CreateOrderTrackRequest request1 = CreateOrderTrackRequest.builder()
                .orderId("order-1")
                .status(Status.NEW)
                .notes("Order placed")
                .build();

        CreateOrderTrackRequest request2 = CreateOrderTrackRequest.builder()
                .orderId("order-1")
                .status(Status.PROCESSING)
                .notes("Order processing")
                .build();

        // When
        OrderTrackResponse result1 = orderTrackService.createOrderTrack(request1);
        OrderTrackResponse result2 = orderTrackService.createOrderTrack(request2);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals("track-1", result1.getId());
        assertEquals("track-2", result2.getId());
        verify(orderRepository, times(2)).findById("order-1");
        verify(orderTrackRepository, times(2)).save(any(OrderTrack.class));
    }
}
