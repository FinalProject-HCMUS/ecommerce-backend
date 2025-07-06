package com.hcmus.ecommerce_backend.unit.product.service;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.hcmus.ecommerce_backend.product.exception.SizeAlreadyExistsException;
import com.hcmus.ecommerce_backend.product.exception.SizeNotFoundException;
import com.hcmus.ecommerce_backend.product.model.dto.request.size.CreateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.size.UpdateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.SizeResponse;
import com.hcmus.ecommerce_backend.product.model.entity.Size;
import com.hcmus.ecommerce_backend.product.model.mapper.SizeMapper;
import com.hcmus.ecommerce_backend.product.repository.SizeRepository;
import com.hcmus.ecommerce_backend.product.service.impl.SizeServiceImpl;

@ExtendWith(MockitoExtension.class)
class SizeServiceImplTest {

    @Mock
    private SizeRepository sizeRepository;

    @Mock
    private SizeMapper sizeMapper;

    @InjectMocks
    private SizeServiceImpl sizeService;

    private Size size;
    private SizeResponse sizeResponse;
    private CreateSizeRequest createSizeRequest;
    private UpdateSizeRequest updateSizeRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup Size entity
        size = new Size();
        size.setId("size-1");
        size.setName("Medium");
        size.setMinHeight(160);
        size.setMaxHeight(170);
        size.setMinWeight(50);
        size.setMaxWeight(65);

        // Setup SizeResponse
        sizeResponse = SizeResponse.builder()
                .id("size-1")
                .name("Medium")
                .minHeight(160)
                .maxHeight(170)
                .minWeight(50)
                .maxWeight(65)
                .build();

        // Setup CreateSizeRequest
        createSizeRequest = CreateSizeRequest.builder()
                .name("Medium")
                .minHeight(160)
                .maxHeight(170)
                .minWeight(50)
                .maxWeight(65)
                .build();

        // Setup UpdateSizeRequest
        updateSizeRequest = UpdateSizeRequest.builder()
                .name("Large")
                .minHeight(170)
                .maxHeight(180)
                .minWeight(65)
                .maxWeight(80)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void searchSizes_WithAllFilters_Success() {
        // Given
        String keyword = "Medium";
        Integer minHeight = 160;
        Integer maxHeight = 170;
        Integer minWeight = 50;
        Integer maxWeight = 65;

        List<Size> sizes = Arrays.asList(size);
        Page<Size> sizePage = new PageImpl<>(sizes, pageable, 1);
        when(sizeRepository.searchSizes(keyword, minHeight, maxHeight, minWeight, maxWeight, pageable))
                .thenReturn(sizePage);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);

        // When
        Page<SizeResponse> result = sizeService.searchSizes(pageable, keyword, minHeight, maxHeight, minWeight, maxWeight);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(sizeResponse, result.getContent().get(0));
        verify(sizeRepository).searchSizes(keyword, minHeight, maxHeight, minWeight, maxWeight, pageable);
        verify(sizeMapper).toResponse(size);
    }

    @Test
    void searchSizes_WithNullKeyword_Success() {
        // Given
        List<Size> sizes = Arrays.asList(size);
        Page<Size> sizePage = new PageImpl<>(sizes, pageable, 1);
        when(sizeRepository.searchSizes(null, null, null, null, null, pageable))
                .thenReturn(sizePage);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);

        // When
        Page<SizeResponse> result = sizeService.searchSizes(pageable, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(sizeRepository).searchSizes(null, null, null, null, null, pageable);
    }

    @Test
    void searchSizes_WithEmptyKeyword_SanitizesToNull() {
        // Given
        String keyword = "   ";
        List<Size> sizes = Arrays.asList(size);
        Page<Size> sizePage = new PageImpl<>(sizes, pageable, 1);
        when(sizeRepository.searchSizes(null, null, null, null, null, pageable))
                .thenReturn(sizePage);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);

        // When
        Page<SizeResponse> result = sizeService.searchSizes(pageable, keyword, null, null, null, null);

        // Then
        assertNotNull(result);
        verify(sizeRepository).searchSizes(null, null, null, null, null, pageable);
    }

    @Test
    void searchSizes_EmptyResult() {
        // Given
        Page<Size> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(sizeRepository.searchSizes(any(), any(), any(), any(), any(), eq(pageable)))
                .thenReturn(emptyPage);

        // When
        Page<SizeResponse> result = sizeService.searchSizes(pageable, "NonExistent", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void searchSizes_DatabaseError_ReturnsEmptyPage() {
        // Given
        when(sizeRepository.searchSizes(any(), any(), any(), any(), any(), eq(pageable)))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When
        Page<SizeResponse> result = sizeService.searchSizes(pageable, "test", null, null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sizeRepository).searchSizes(any(), any(), any(), any(), any(), eq(pageable));
    }

    @Test
    void searchSizes_UnexpectedError_ThrowsException() {
        // Given
        when(sizeRepository.searchSizes(any(), any(), any(), any(), any(), eq(pageable)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> sizeService.searchSizes(pageable, "test", null, null, null, null));
    }

    @Test
    void getSizeById_Success() {
        // Given
        String id = "size-1";
        when(sizeRepository.findById(id)).thenReturn(Optional.of(size));
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);

        // When
        SizeResponse result = sizeService.getSizeById(id);

        // Then
        assertNotNull(result);
        assertEquals(sizeResponse, result);
        verify(sizeRepository).findById(id);
        verify(sizeMapper).toResponse(size);
    }

    @Test
    void getSizeById_NotFound() {
        // Given
        String id = "non-existent";
        when(sizeRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(SizeNotFoundException.class, () -> sizeService.getSizeById(id));
        verify(sizeRepository).findById(id);
    }

    @Test
    void getSizeById_DatabaseError() {
        // Given
        String id = "size-1";
        when(sizeRepository.findById(id)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> sizeService.getSizeById(id));
        verify(sizeRepository).findById(id);
    }

    @Test
    void getSizeById_UnexpectedError() {
        // Given
        String id = "size-1";
        when(sizeRepository.findById(id)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> sizeService.getSizeById(id));
        verify(sizeRepository).findById(id);
    }

    @Test
    void createSize_Success() {
        // Given
        when(sizeRepository.existsByName("Medium")).thenReturn(false);
        when(sizeMapper.toEntity(createSizeRequest)).thenReturn(size);
        when(sizeRepository.save(size)).thenReturn(size);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);

        // When
        SizeResponse result = sizeService.createSize(createSizeRequest);

        // Then
        assertNotNull(result);
        assertEquals(sizeResponse, result);
        verify(sizeRepository).existsByName("Medium");
        verify(sizeMapper).toEntity(createSizeRequest);
        verify(sizeRepository).save(size);
        verify(sizeMapper).toResponse(size);
    }

    @Test
    void createSize_AlreadyExists() {
        // Given
        when(sizeRepository.existsByName("Medium")).thenReturn(true);

        // When & Then
        assertThrows(SizeAlreadyExistsException.class, () -> sizeService.createSize(createSizeRequest));
        verify(sizeRepository).existsByName("Medium");
        verify(sizeRepository, never()).save(any());
    }

    @Test
    void createSize_DatabaseError() {
        // Given
        when(sizeRepository.existsByName("Medium")).thenReturn(false);
        when(sizeMapper.toEntity(createSizeRequest)).thenReturn(size);
        when(sizeRepository.save(size)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> sizeService.createSize(createSizeRequest));
        verify(sizeRepository).save(size);
    }

    @Test
    void createSize_UnexpectedError() {
        // Given
        when(sizeRepository.existsByName("Medium")).thenReturn(false);
        when(sizeMapper.toEntity(createSizeRequest)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> sizeService.createSize(createSizeRequest));
        verify(sizeRepository).existsByName("Medium");
        verify(sizeMapper).toEntity(createSizeRequest);
    }

    @Test
    void createMultipleSizes_Success() {
        // Given
        CreateSizeRequest request2 = CreateSizeRequest.builder()
                .name("Large")
                .minHeight(170)
                .maxHeight(180)
                .minWeight(65)
                .maxWeight(80)
                .build();

        Size size2 = new Size();
        size2.setId("size-2");
        size2.setName("Large");

        SizeResponse response2 = SizeResponse.builder()
                .id("size-2")
                .name("Large")
                .build();

        List<CreateSizeRequest> requests = Arrays.asList(createSizeRequest, request2);
        List<Size> sizes = Arrays.asList(size, size2);
        List<Size> savedSizes = Arrays.asList(size, size2);

        when(sizeRepository.existsByName("Medium")).thenReturn(false);
        when(sizeRepository.existsByName("Large")).thenReturn(false);
        when(sizeMapper.toEntity(createSizeRequest)).thenReturn(size);
        when(sizeMapper.toEntity(request2)).thenReturn(size2);
        when(sizeRepository.saveAll(sizes)).thenReturn(savedSizes);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);
        when(sizeMapper.toResponse(size2)).thenReturn(response2);

        // When
        List<SizeResponse> result = sizeService.createMultipleSizes(requests);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(sizeResponse));
        assertTrue(result.contains(response2));
        verify(sizeRepository).existsByName("Medium");
        verify(sizeRepository).existsByName("Large");
        verify(sizeRepository).saveAll(sizes);
    }

    @Test
    void createMultipleSizes_OneAlreadyExists() {
        // Given
        CreateSizeRequest request2 = CreateSizeRequest.builder()
                .name("Large")
                .build();

        List<CreateSizeRequest> requests = Arrays.asList(createSizeRequest, request2);

        when(sizeRepository.existsByName("Medium")).thenReturn(false);
        when(sizeRepository.existsByName("Large")).thenReturn(true);

        // When & Then
        assertThrows(SizeAlreadyExistsException.class, () -> sizeService.createMultipleSizes(requests));
        verify(sizeRepository).existsByName("Medium");
        verify(sizeRepository).existsByName("Large");
        verify(sizeRepository, never()).saveAll(any());
    }

    @Test
    void createMultipleSizes_EmptyList() {
        // Given
        List<CreateSizeRequest> requests = Collections.emptyList();
        when(sizeRepository.saveAll(Collections.emptyList())).thenReturn(Collections.emptyList());

        // When
        List<SizeResponse> result = sizeService.createMultipleSizes(requests);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sizeRepository).saveAll(Collections.emptyList());
    }

    @Test
    void createMultipleSizes_DatabaseError() {
        // Given
        List<CreateSizeRequest> requests = Arrays.asList(createSizeRequest);
        List<Size> sizes = Arrays.asList(size);

        when(sizeRepository.existsByName("Medium")).thenReturn(false);
        when(sizeMapper.toEntity(createSizeRequest)).thenReturn(size);
        when(sizeRepository.saveAll(sizes)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> sizeService.createMultipleSizes(requests));
        verify(sizeRepository).saveAll(sizes);
    }

    @Test
    void createMultipleSizes_UnexpectedError() {
        // Given
        List<CreateSizeRequest> requests = Arrays.asList(createSizeRequest);
        when(sizeRepository.existsByName("Medium")).thenReturn(false);
        when(sizeMapper.toEntity(createSizeRequest)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> sizeService.createMultipleSizes(requests));
        verify(sizeRepository).existsByName("Medium");
        verify(sizeMapper).toEntity(createSizeRequest);
    }

    @Test
    void updateSize_Success() {
        // Given
        String id = "size-1";
        when(sizeRepository.findById(id)).thenReturn(Optional.of(size));
        when(sizeRepository.existsByName("Large")).thenReturn(false);
        when(sizeRepository.save(size)).thenReturn(size);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);

        // When
        SizeResponse result = sizeService.updateSize(id, updateSizeRequest);

        // Then
        assertNotNull(result);
        assertEquals(sizeResponse, result);
        verify(sizeRepository).findById(id);
        verify(sizeRepository).existsByName("Large");
        verify(sizeMapper).updateEntity(updateSizeRequest, size);
        verify(sizeRepository).save(size);
        verify(sizeMapper).toResponse(size);
    }

    @Test
    void updateSize_SameName_NoNameCheck() {
        // Given
        String id = "size-1";
        updateSizeRequest.setName("Medium"); // Same as existing name
        when(sizeRepository.findById(id)).thenReturn(Optional.of(size));
        when(sizeRepository.save(size)).thenReturn(size);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);

        // When
        SizeResponse result = sizeService.updateSize(id, updateSizeRequest);

        // Then
        assertNotNull(result);
        verify(sizeRepository).findById(id);
        verify(sizeRepository, never()).existsByName(anyString());
        verify(sizeRepository).save(size);
    }

    @Test
    void updateSize_NotFound() {
        // Given
        String id = "non-existent";
        when(sizeRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(SizeNotFoundException.class, () -> sizeService.updateSize(id, updateSizeRequest));
        verify(sizeRepository).findById(id);
        verify(sizeRepository, never()).save(any());
    }

    @Test
    void updateSize_NewNameAlreadyExists() {
        // Given
        String id = "size-1";
        when(sizeRepository.findById(id)).thenReturn(Optional.of(size));
        when(sizeRepository.existsByName("Large")).thenReturn(true);

        // When & Then
        assertThrows(SizeAlreadyExistsException.class, () -> sizeService.updateSize(id, updateSizeRequest));
        verify(sizeRepository).findById(id);
        verify(sizeRepository).existsByName("Large");
        verify(sizeRepository, never()).save(any());
    }

    @Test
    void updateSize_DatabaseError() {
        // Given
        String id = "size-1";
        when(sizeRepository.findById(id)).thenReturn(Optional.of(size));
        when(sizeRepository.existsByName("Large")).thenReturn(false);
        when(sizeRepository.save(size)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> sizeService.updateSize(id, updateSizeRequest));
        verify(sizeRepository).save(size);
    }

    @Test
    void updateSize_UnexpectedError() {
        // Given
        String id = "size-1";
        when(sizeRepository.findById(id)).thenReturn(Optional.of(size));
        when(sizeRepository.existsByName("Large")).thenReturn(false);
        doThrow(new RuntimeException("Mapping error")).when(sizeMapper).updateEntity(updateSizeRequest, size);

        // When & Then
        assertThrows(RuntimeException.class, () -> sizeService.updateSize(id, updateSizeRequest));
        verify(sizeRepository).findById(id);
        verify(sizeMapper).updateEntity(updateSizeRequest, size);
    }

    @Test
    void deleteSize_Success() {
        // Given
        String id = "size-1";
        when(sizeRepository.existsById(id)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> sizeService.deleteSize(id));

        // Then
        verify(sizeRepository).existsById(id);
        verify(sizeRepository).deleteById(id);
    }

    @Test
    void deleteSize_NotFound() {
        // Given
        String id = "non-existent";
        when(sizeRepository.existsById(id)).thenReturn(false);

        // When & Then
        assertThrows(SizeNotFoundException.class, () -> sizeService.deleteSize(id));
        verify(sizeRepository).existsById(id);
        verify(sizeRepository, never()).deleteById(id);
    }

    @Test
    void deleteSize_DatabaseError() {
        // Given
        String id = "size-1";
        when(sizeRepository.existsById(id)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Database error")).when(sizeRepository).deleteById(id);

        // When & Then
        assertThrows(DataAccessException.class, () -> sizeService.deleteSize(id));
        verify(sizeRepository).existsById(id);
        verify(sizeRepository).deleteById(id);
    }

    @Test
    void deleteSize_UnexpectedError() {
        // Given
        String id = "size-1";
        when(sizeRepository.existsById(id)).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(sizeRepository).deleteById(id);

        // When & Then
        assertThrows(RuntimeException.class, () -> sizeService.deleteSize(id));
        verify(sizeRepository).existsById(id);
        verify(sizeRepository).deleteById(id);
    }

    @Test
    void findSizeById_Success() {
        // Given
        String id = "size-1";
        when(sizeRepository.findById(id)).thenReturn(Optional.of(size));

        // When
        Size result = sizeService.findSizeById(id);

        // Then
        assertNotNull(result);
        assertEquals(size, result);
        verify(sizeRepository).findById(id);
    }

    @Test
    void findSizeById_NotFound() {
        // Given
        String id = "non-existent";
        when(sizeRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(SizeNotFoundException.class, () -> sizeService.findSizeById(id));
        verify(sizeRepository).findById(id);
    }

    @Test
    void doesSizeExistById_True() {
        // Given
        String id = "size-1";
        when(sizeRepository.existsById(id)).thenReturn(true);

        // When
        boolean result = sizeService.doesSizeExistById(id);

        // Then
        assertTrue(result);
        verify(sizeRepository).existsById(id);
    }

    @Test
    void doesSizeExistById_False() {
        // Given
        String id = "non-existent";
        when(sizeRepository.existsById(id)).thenReturn(false);

        // When
        boolean result = sizeService.doesSizeExistById(id);

        // Then
        assertFalse(result);
        verify(sizeRepository).existsById(id);
    }

    @Test
    void checkSizeNameExists_Exists() {
        // Given
        String name = "Medium";
        when(sizeRepository.existsByName(name)).thenReturn(true);

        // When & Then
        assertThrows(SizeAlreadyExistsException.class, () -> sizeService.checkSizeNameExists(name));
        verify(sizeRepository).existsByName(name);
    }

    @Test
    void checkSizeNameExists_NotExists() {
        // Given
        String name = "Medium";
        when(sizeRepository.existsByName(name)).thenReturn(false);

        // When
        assertDoesNotThrow(() -> sizeService.checkSizeNameExists(name));

        // Then
        verify(sizeRepository).existsByName(name);
    }

    @Test
    void searchSizes_WithPartialFilters_Success() {
        // Given
        String keyword = "Med";
        Integer minHeight = 150;
        List<Size> sizes = Arrays.asList(size);
        Page<Size> sizePage = new PageImpl<>(sizes, pageable, 1);
        when(sizeRepository.searchSizes(keyword, minHeight, null, null, null, pageable))
                .thenReturn(sizePage);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);

        // When
        Page<SizeResponse> result = sizeService.searchSizes(pageable, keyword, minHeight, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(sizeRepository).searchSizes(keyword, minHeight, null, null, null, pageable);
    }

    @Test
    void searchSizes_WithBlankKeyword_SanitizesToNull() {
        // Given
        String keyword = "";
        List<Size> sizes = Arrays.asList(size);
        Page<Size> sizePage = new PageImpl<>(sizes, pageable, 1);
        when(sizeRepository.searchSizes(null, null, null, null, null, pageable))
                .thenReturn(sizePage);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);

        // When
        Page<SizeResponse> result = sizeService.searchSizes(pageable, keyword, null, null, null, null);

        // Then
        assertNotNull(result);
        verify(sizeRepository).searchSizes(null, null, null, null, null, pageable);
    }

    @Test
    void createMultipleSizes_SingleSize() {
        // Given
        List<CreateSizeRequest> requests = Arrays.asList(createSizeRequest);
        List<Size> sizes = Arrays.asList(size);
        List<Size> savedSizes = Arrays.asList(size);

        when(sizeRepository.existsByName("Medium")).thenReturn(false);
        when(sizeMapper.toEntity(createSizeRequest)).thenReturn(size);
        when(sizeRepository.saveAll(sizes)).thenReturn(savedSizes);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);

        // When
        List<SizeResponse> result = sizeService.createMultipleSizes(requests);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sizeResponse, result.get(0));
        verify(sizeRepository).saveAll(sizes);
    }

    @Test
    void searchSizes_MultipleResults() {
        // Given
        Size size2 = new Size();
        size2.setId("size-2");
        size2.setName("Large");

        SizeResponse response2 = SizeResponse.builder()
                .id("size-2")
                .name("Large")
                .build();

        List<Size> sizes = Arrays.asList(size, size2);
        Page<Size> sizePage = new PageImpl<>(sizes, pageable, 2);
        when(sizeRepository.searchSizes(any(), any(), any(), any(), any(), eq(pageable)))
                .thenReturn(sizePage);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);
        when(sizeMapper.toResponse(size2)).thenReturn(response2);

        // When
        Page<SizeResponse> result = sizeService.searchSizes(pageable, "size", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        verify(sizeMapper, times(2)).toResponse(any(Size.class));
    }

    @Test
    void updateSize_DatabaseErrorOnFind() {
        // Given
        String id = "size-1";
        when(sizeRepository.findById(id)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> sizeService.updateSize(id, updateSizeRequest));
        verify(sizeRepository).findById(id);
    }

    @Test
    void createSize_MapperError() {
        // Given
        when(sizeRepository.existsByName("Medium")).thenReturn(false);
        when(sizeMapper.toResponse(any())).thenThrow(new RuntimeException("Response mapping error"));
        when(sizeMapper.toEntity(createSizeRequest)).thenReturn(size);
        when(sizeRepository.save(size)).thenReturn(size);

        // When & Then
        assertThrows(RuntimeException.class, () -> sizeService.createSize(createSizeRequest));
        verify(sizeMapper).toResponse(size);
    }
}
