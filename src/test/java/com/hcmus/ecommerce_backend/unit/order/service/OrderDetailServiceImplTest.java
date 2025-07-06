package com.hcmus.ecommerce_backend.unit.order.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import com.hcmus.ecommerce_backend.order.exception.OrderDetailNotFoundException;
import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderDetailResponse;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderDetailWithProductResponse;
import com.hcmus.ecommerce_backend.order.model.entity.OrderDetail;
import com.hcmus.ecommerce_backend.order.model.mapper.OrderDetailMapper;
import com.hcmus.ecommerce_backend.order.repository.OrderDetailRepository;
import com.hcmus.ecommerce_backend.order.service.impl.OrderDetailServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderDetailServiceImplTest {

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private OrderDetailMapper orderDetailMapper;

    @InjectMocks
    private OrderDetailServiceImpl orderDetailService;

    private OrderDetail orderDetail;
    private OrderDetailResponse orderDetailResponse;
    private CreateOrderDetailRequest createRequest;
    private UpdateOrderDetailRequest updateRequest;

    @BeforeEach
    void setUp() {
        orderDetail = new OrderDetail();
        orderDetail.setId("detail-1");

        orderDetailResponse = OrderDetailResponse.builder()
                .build();

        createRequest = CreateOrderDetailRequest.builder()
                .orderId("order-1")
                .itemId("item-1")
                .quantity(2)
                .unitPrice(10.0)
                .build();

        updateRequest = UpdateOrderDetailRequest.builder()
                .quantity(3)
                .unitPrice(15.0)
                .build();
    }

    @Test
    void getAllOrderDetails_Success() {
        // Given
        List<OrderDetail> orderDetails = Arrays.asList(orderDetail);
        List<OrderDetailResponse> responses = Arrays.asList(orderDetailResponse);

        when(orderDetailRepository.findAll()).thenReturn(orderDetails);
        when(orderDetailMapper.toResponse(orderDetail)).thenReturn(orderDetailResponse);

        // When
        List<OrderDetailResponse> result = orderDetailService.getAllOrderDetails();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(orderDetailResponse, result.get(0));
        verify(orderDetailRepository).findAll();
        verify(orderDetailMapper).toResponse(orderDetail);
    }

    @Test
    void getAllOrderDetails_EmptyList() {
        // Given
        when(orderDetailRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<OrderDetailResponse> result = orderDetailService.getAllOrderDetails();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderDetailRepository).findAll();
    }

    @Test
    void getAllOrderDetails_DatabaseError() {
        // Given
        when(orderDetailRepository.findAll()).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> orderDetailService.getAllOrderDetails());
        verify(orderDetailRepository).findAll();
    }

    @Test
    void getOrderDetailById_Success() {
        // Given
        String id = "detail-1";
        when(orderDetailRepository.findById(id)).thenReturn(Optional.of(orderDetail));
        when(orderDetailMapper.toResponse(orderDetail)).thenReturn(orderDetailResponse);

        // When
        OrderDetailResponse result = orderDetailService.getOrderDetailById(id);

        // Then
        assertNotNull(result);
        assertEquals(orderDetailResponse, result);
        verify(orderDetailRepository).findById(id);
        verify(orderDetailMapper).toResponse(orderDetail);
    }

    @Test
    void getOrderDetailById_NotFound() {
        // Given
        String id = "non-existent";
        when(orderDetailRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderDetailNotFoundException.class, () -> orderDetailService.getOrderDetailById(id));
        verify(orderDetailRepository).findById(id);
    }

    @Test
    void getOrderDetailById_DatabaseError() {
        // Given
        String id = "detail-1";
        when(orderDetailRepository.findById(id)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> orderDetailService.getOrderDetailById(id));
        verify(orderDetailRepository).findById(id);
    }

    @Test
    void createOrder_Success() {
        // Given
        when(orderDetailMapper.toEntity(createRequest)).thenReturn(orderDetail);
        when(orderDetailRepository.save(orderDetail)).thenReturn(orderDetail);
        when(orderDetailMapper.toResponse(orderDetail)).thenReturn(orderDetailResponse);

        // When
        OrderDetailResponse result = orderDetailService.createOrder(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(orderDetailResponse, result);
        verify(orderDetailMapper).toEntity(createRequest);
        verify(orderDetailRepository).save(orderDetail);
        verify(orderDetailMapper).toResponse(orderDetail);
    }

    @Test
    void createOrder_DatabaseError() {
        // Given
        when(orderDetailMapper.toEntity(createRequest)).thenReturn(orderDetail);
        when(orderDetailRepository.save(orderDetail)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> orderDetailService.createOrder(createRequest));
        verify(orderDetailMapper).toEntity(createRequest);
        verify(orderDetailRepository).save(orderDetail);
    }

    @Test
    void updateOrder_Success() {
        // Given
        String id = "detail-1";
        when(orderDetailRepository.findById(id)).thenReturn(Optional.of(orderDetail));
        when(orderDetailRepository.save(orderDetail)).thenReturn(orderDetail);
        when(orderDetailMapper.toResponse(orderDetail)).thenReturn(orderDetailResponse);

        // When
        OrderDetailResponse result = orderDetailService.updateOrder(id, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(orderDetailResponse, result);
        verify(orderDetailRepository).findById(id);
        verify(orderDetailMapper).updateEntity(updateRequest, orderDetail);
        verify(orderDetailRepository).save(orderDetail);
        verify(orderDetailMapper).toResponse(orderDetail);
    }

    @Test
    void updateOrder_NotFound() {
        // Given
        String id = "non-existent";
        when(orderDetailRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderDetailNotFoundException.class, () -> orderDetailService.updateOrder(id, updateRequest));
        verify(orderDetailRepository).findById(id);
        verify(orderDetailMapper, never()).updateEntity(any(), any());
    }

    @Test
    void updateOrder_DatabaseError() {
        // Given
        String id = "detail-1";
        when(orderDetailRepository.findById(id)).thenReturn(Optional.of(orderDetail));
        when(orderDetailRepository.save(orderDetail)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> orderDetailService.updateOrder(id, updateRequest));
        verify(orderDetailRepository).findById(id);
        verify(orderDetailRepository).save(orderDetail);
    }

    @Test
    void deleteOrderDetail_Success() {
        // Given
        String id = "detail-1";
        when(orderDetailRepository.existsById(id)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> orderDetailService.deleteOrderDetail(id));

        // Then
        verify(orderDetailRepository).existsById(id);
        verify(orderDetailRepository).deleteById(id);
    }

    @Test
    void deleteOrderDetail_NotFound() {
        // Given
        String id = "non-existent";
        when(orderDetailRepository.existsById(id)).thenReturn(false);

        // When & Then
        assertThrows(OrderDetailNotFoundException.class, () -> orderDetailService.deleteOrderDetail(id));
        verify(orderDetailRepository).existsById(id);
        verify(orderDetailRepository, never()).deleteById(id);
    }

    @Test
    void deleteOrderDetail_DatabaseError() {
        // Given
        String id = "detail-1";
        when(orderDetailRepository.existsById(id)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Database error")).when(orderDetailRepository).deleteById(id);

        // When & Then
        assertThrows(DataAccessException.class, () -> orderDetailService.deleteOrderDetail(id));
        verify(orderDetailRepository).existsById(id);
        verify(orderDetailRepository).deleteById(id);
    }

    @Test
    void getOrderDetailsWithProductByOrderId_Success() {
        // Given
        String orderId = "order-1";
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("id", "detail-1");
        rawData.put("product_name", "Test Product");
        
        List<Map<String, Object>> rawResults = Arrays.asList(rawData);
        
        OrderDetailWithProductResponse response = OrderDetailWithProductResponse.builder()
                .id("detail-1")
                .build();

        when(orderDetailRepository.findOrderDetailsWithProductByOrderId(orderId)).thenReturn(rawResults);
        when(orderDetailMapper.mapToOrderDetailWithProductResponse(rawData)).thenReturn(response);

        // When
        List<OrderDetailWithProductResponse> result = orderDetailService.getOrderDetailsWithProductByOrderId(orderId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(response, result.get(0));
        verify(orderDetailRepository).findOrderDetailsWithProductByOrderId(orderId);
        verify(orderDetailMapper).mapToOrderDetailWithProductResponse(rawData);
    }

    @Test
    void getOrderDetailsWithProductByOrderId_NoResults() {
        // Given
        String orderId = "order-1";
        when(orderDetailRepository.findOrderDetailsWithProductByOrderId(orderId)).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(OrderDetailNotFoundException.class, 
                () -> orderDetailService.getOrderDetailsWithProductByOrderId(orderId));
        verify(orderDetailRepository).findOrderDetailsWithProductByOrderId(orderId);
    }

    @Test
    void getOrderDetailsWithProductByOrderId_DatabaseError() {
        // Given
        String orderId = "order-1";
        when(orderDetailRepository.findOrderDetailsWithProductByOrderId(orderId))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, 
                () -> orderDetailService.getOrderDetailsWithProductByOrderId(orderId));
        verify(orderDetailRepository).findOrderDetailsWithProductByOrderId(orderId);
    }

    @Test
    void addOrderDetails_Success() {
        // Given
        List<CreateOrderDetailRequest> requests = Arrays.asList(createRequest);
        List<OrderDetail> orderDetails = Arrays.asList(orderDetail);
        List<OrderDetailResponse> responses = Arrays.asList(orderDetailResponse);

        when(orderDetailMapper.toEntity(createRequest)).thenReturn(orderDetail);
        when(orderDetailRepository.saveAll(orderDetails)).thenReturn(orderDetails);
        when(orderDetailMapper.toResponse(orderDetail)).thenReturn(orderDetailResponse);

        // When
        List<OrderDetailResponse> result = orderDetailService.addOrderDetails(requests);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(orderDetailResponse, result.get(0));
        verify(orderDetailMapper).toEntity(createRequest);
        verify(orderDetailRepository).saveAll(orderDetails);
        verify(orderDetailMapper).toResponse(orderDetail);
    }

    @Test
    void addOrderDetails_EmptyList() {
        // Given
        List<CreateOrderDetailRequest> requests = Collections.emptyList();
        when(orderDetailRepository.saveAll(Collections.emptyList())).thenReturn(Collections.emptyList());

        // When
        List<OrderDetailResponse> result = orderDetailService.addOrderDetails(requests);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderDetailRepository).saveAll(Collections.emptyList());
    }

    @Test
    void addOrderDetails_DatabaseError() {
        // Given
        List<CreateOrderDetailRequest> requests = Arrays.asList(createRequest);
        List<OrderDetail> orderDetails = Arrays.asList(orderDetail);

        when(orderDetailMapper.toEntity(createRequest)).thenReturn(orderDetail);
        when(orderDetailRepository.saveAll(orderDetails)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> orderDetailService.addOrderDetails(requests));
        verify(orderDetailMapper).toEntity(createRequest);
        verify(orderDetailRepository).saveAll(orderDetails);
    }

    @Test
    void addOrderDetails_MultipleItems() {
        // Given
        CreateOrderDetailRequest request2 = CreateOrderDetailRequest.builder()
                .orderId("order-1")
                .itemId("item-2")
                .quantity(1)
                .unitPrice(20.0)
                .build();

        OrderDetail orderDetail2 = new OrderDetail();
        orderDetail2.setId("detail-2");

        OrderDetailResponse response2 = OrderDetailResponse.builder()
                .build();

        List<CreateOrderDetailRequest> requests = Arrays.asList(createRequest, request2);
        List<OrderDetail> orderDetails = Arrays.asList(orderDetail, orderDetail2);

        when(orderDetailMapper.toEntity(createRequest)).thenReturn(orderDetail);
        when(orderDetailMapper.toEntity(request2)).thenReturn(orderDetail2);
        when(orderDetailRepository.saveAll(orderDetails)).thenReturn(orderDetails);
        when(orderDetailMapper.toResponse(orderDetail)).thenReturn(orderDetailResponse);
        when(orderDetailMapper.toResponse(orderDetail2)).thenReturn(response2);

        // When
        List<OrderDetailResponse> result = orderDetailService.addOrderDetails(requests);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(orderDetailResponse));
        assertTrue(result.contains(response2));
        verify(orderDetailMapper).toEntity(createRequest);
        verify(orderDetailMapper).toEntity(request2);
        verify(orderDetailRepository).saveAll(orderDetails);
        verify(orderDetailMapper).toResponse(orderDetail);
        verify(orderDetailMapper).toResponse(orderDetail2);
    }

    @Test
    void getAllOrderDetails_UnexpectedError() {
        // Given
        when(orderDetailRepository.findAll()).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> orderDetailService.getAllOrderDetails());
        verify(orderDetailRepository).findAll();
    }

    @Test
    void createOrder_UnexpectedError() {
        // Given
        when(orderDetailMapper.toEntity(createRequest)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> orderDetailService.createOrder(createRequest));
        verify(orderDetailMapper).toEntity(createRequest);
    }

    @Test
    void getOrderDetailsWithProductByOrderId_UnexpectedError() {
        // Given
        String orderId = "order-1";
        when(orderDetailRepository.findOrderDetailsWithProductByOrderId(orderId))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> orderDetailService.getOrderDetailsWithProductByOrderId(orderId));
        verify(orderDetailRepository).findOrderDetailsWithProductByOrderId(orderId);
    }
}
