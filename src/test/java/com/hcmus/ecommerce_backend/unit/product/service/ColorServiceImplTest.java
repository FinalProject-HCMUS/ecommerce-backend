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
import com.hcmus.ecommerce_backend.product.exception.ColorAlreadyExistsException;
import com.hcmus.ecommerce_backend.product.exception.ColorNotFoundException;
import com.hcmus.ecommerce_backend.product.model.dto.request.color.CreateColorRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.color.UpdateColorRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ColorResponse;
import com.hcmus.ecommerce_backend.product.model.entity.Color;
import com.hcmus.ecommerce_backend.product.model.mapper.ColorMapper;
import com.hcmus.ecommerce_backend.product.repository.ColorRepository;
import com.hcmus.ecommerce_backend.product.service.impl.ColorServiceImpl;

@ExtendWith(MockitoExtension.class)
class ColorServiceImplTest {

    @Mock
    private ColorRepository colorRepository;

    @Mock
    private ColorMapper colorMapper;

    @InjectMocks
    private ColorServiceImpl colorService;

    private Color color;
    private ColorResponse colorResponse;
    private CreateColorRequest createColorRequest;
    private UpdateColorRequest updateColorRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup Color entity
        color = new Color();
        color.setId("color-1");
        color.setName("Red");
        color.setCode("#FF0000");

        // Setup ColorResponse
        colorResponse = ColorResponse.builder()
                .id("color-1")
                .name("Red")
                .code("#FF0000")
                .build();

        // Setup CreateColorRequest
        createColorRequest = CreateColorRequest.builder()
                .name("Red")
                .code("#FF0000")
                .build();

        // Setup UpdateColorRequest
        updateColorRequest = UpdateColorRequest.builder()
                .name("Blue")
                .code("#0000FF")
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllColors_Success() {
        // Given
        List<Color> colors = Arrays.asList(color);
        Page<Color> colorPage = new PageImpl<>(colors, pageable, 1);
        when(colorRepository.findAll(pageable)).thenReturn(colorPage);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);

        // When
        Page<ColorResponse> result = colorService.getAllColors(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(colorResponse, result.getContent().get(0));
        verify(colorRepository).findAll(pageable);
        verify(colorMapper).toResponse(color);
    }

    @Test
    void getAllColors_EmptyResult() {
        // Given
        Page<Color> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(colorRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        Page<ColorResponse> result = colorService.getAllColors(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(colorRepository).findAll(pageable);
    }

    @Test
    void getAllColors_DatabaseError() {
        // Given
        when(colorRepository.findAll(pageable)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When
        Page<ColorResponse> result = colorService.getAllColors(pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(colorRepository).findAll(pageable);
    }

    @Test
    void getAllColors_UnexpectedError() {
        // Given
        when(colorRepository.findAll(pageable)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> colorService.getAllColors(pageable));
        verify(colorRepository).findAll(pageable);
    }

    @Test
    void searchColors_WithKeyword() {
        // Given
        String keyword = "Red";
        List<Color> colors = Arrays.asList(color);
        Page<Color> colorPage = new PageImpl<>(colors, pageable, 1);
        when(colorRepository.findByNameContainingIgnoreCase(keyword, pageable)).thenReturn(colorPage);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);

        // When
        Page<ColorResponse> result = colorService.searchColors(pageable, keyword);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(colorResponse, result.getContent().get(0));
        verify(colorRepository).findByNameContainingIgnoreCase(keyword, pageable);
        verify(colorMapper).toResponse(color);
    }

    @Test
    void searchColors_WithKeywordTrimmed() {
        // Given
        String keyword = "  Red  ";
        String trimmedKeyword = "Red";
        List<Color> colors = Arrays.asList(color);
        Page<Color> colorPage = new PageImpl<>(colors, pageable, 1);
        when(colorRepository.findByNameContainingIgnoreCase(trimmedKeyword, pageable)).thenReturn(colorPage);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);

        // When
        Page<ColorResponse> result = colorService.searchColors(pageable, keyword);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(colorRepository).findByNameContainingIgnoreCase(trimmedKeyword, pageable);
    }

    @Test
    void searchColors_NullKeyword() {
        // Given
        List<Color> colors = Arrays.asList(color);
        Page<Color> colorPage = new PageImpl<>(colors, pageable, 1);
        when(colorRepository.findAll(pageable)).thenReturn(colorPage);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);

        // When
        Page<ColorResponse> result = colorService.searchColors(pageable, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(colorRepository).findAll(pageable);
        verify(colorRepository, never()).findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    void searchColors_EmptyKeyword() {
        // Given
        String keyword = "";
        List<Color> colors = Arrays.asList(color);
        Page<Color> colorPage = new PageImpl<>(colors, pageable, 1);
        when(colorRepository.findAll(pageable)).thenReturn(colorPage);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);

        // When
        Page<ColorResponse> result = colorService.searchColors(pageable, keyword);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(colorRepository).findAll(pageable);
        verify(colorRepository, never()).findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    void searchColors_WhitespaceKeyword() {
        // Given
        String keyword = "   ";
        List<Color> colors = Arrays.asList(color);
        Page<Color> colorPage = new PageImpl<>(colors, pageable, 1);
        when(colorRepository.findAll(pageable)).thenReturn(colorPage);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);

        // When
        Page<ColorResponse> result = colorService.searchColors(pageable, keyword);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(colorRepository).findAll(pageable);
        verify(colorRepository, never()).findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    void searchColors_DatabaseError() {
        // Given
        String keyword = "Red";
        when(colorRepository.findByNameContainingIgnoreCase(keyword, pageable))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When
        Page<ColorResponse> result = colorService.searchColors(pageable, keyword);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(colorRepository).findByNameContainingIgnoreCase(keyword, pageable);
    }

    @Test
    void searchColors_UnexpectedError() {
        // Given
        String keyword = "Red";
        when(colorRepository.findByNameContainingIgnoreCase(keyword, pageable))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> colorService.searchColors(pageable, keyword));
        verify(colorRepository).findByNameContainingIgnoreCase(keyword, pageable);
    }

    @Test
    void getColorById_Success() {
        // Given
        String id = "color-1";
        when(colorRepository.findById(id)).thenReturn(Optional.of(color));
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);

        // When
        ColorResponse result = colorService.getColorById(id);

        // Then
        assertNotNull(result);
        assertEquals(colorResponse, result);
        verify(colorRepository).findById(id);
        verify(colorMapper).toResponse(color);
    }

    @Test
    void getColorById_NotFound() {
        // Given
        String id = "non-existent";
        when(colorRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ColorNotFoundException.class, () -> colorService.getColorById(id));
        verify(colorRepository).findById(id);
    }

    @Test
    void getColorById_DatabaseError() {
        // Given
        String id = "color-1";
        when(colorRepository.findById(id)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> colorService.getColorById(id));
        verify(colorRepository).findById(id);
    }

    @Test
    void getColorById_UnexpectedError() {
        // Given
        String id = "color-1";
        when(colorRepository.findById(id)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> colorService.getColorById(id));
        verify(colorRepository).findById(id);
    }

    @Test
    void createColor_Success() {
        // Given
        when(colorRepository.existsByName("Red")).thenReturn(false);
        when(colorMapper.toEntity(createColorRequest)).thenReturn(color);
        when(colorRepository.save(color)).thenReturn(color);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);

        // When
        ColorResponse result = colorService.createColor(createColorRequest);

        // Then
        assertNotNull(result);
        assertEquals(colorResponse, result);
        verify(colorRepository).existsByName("Red");
        verify(colorMapper).toEntity(createColorRequest);
        verify(colorRepository).save(color);
        verify(colorMapper).toResponse(color);
    }

    @Test
    void createColor_AlreadyExists() {
        // Given
        when(colorRepository.existsByName("Red")).thenReturn(true);

        // When & Then
        assertThrows(ColorAlreadyExistsException.class, () -> colorService.createColor(createColorRequest));
        verify(colorRepository).existsByName("Red");
        verify(colorRepository, never()).save(any());
    }

    @Test
    void createColor_DatabaseError() {
        // Given
        when(colorRepository.existsByName("Red")).thenReturn(false);
        when(colorMapper.toEntity(createColorRequest)).thenReturn(color);
        when(colorRepository.save(color)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> colorService.createColor(createColorRequest));
        verify(colorRepository).save(color);
    }

    @Test
    void createColor_UnexpectedError() {
        // Given
        when(colorRepository.existsByName("Red")).thenReturn(false);
        when(colorMapper.toEntity(createColorRequest)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> colorService.createColor(createColorRequest));
        verify(colorRepository).existsByName("Red");
        verify(colorMapper).toEntity(createColorRequest);
    }

    @Test
    void createMultipleColors_Success() {
        // Given
        CreateColorRequest request2 = CreateColorRequest.builder()
                .name("Blue")
                .code("#0000FF")
                .build();

        Color color2 = new Color();
        color2.setId("color-2");
        color2.setName("Blue");
        color2.setCode("#0000FF");

        ColorResponse response2 = ColorResponse.builder()
                .id("color-2")
                .name("Blue")
                .code("#0000FF")
                .build();

        List<CreateColorRequest> requests = Arrays.asList(createColorRequest, request2);
        List<Color> colors = Arrays.asList(color, color2);
        List<Color> savedColors = Arrays.asList(color, color2);

        when(colorRepository.existsByName("Red")).thenReturn(false);
        when(colorRepository.existsByName("Blue")).thenReturn(false);
        when(colorMapper.toEntity(createColorRequest)).thenReturn(color);
        when(colorMapper.toEntity(request2)).thenReturn(color2);
        when(colorRepository.saveAll(colors)).thenReturn(savedColors);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);
        when(colorMapper.toResponse(color2)).thenReturn(response2);

        // When
        List<ColorResponse> result = colorService.createMultipleColors(requests);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(colorResponse));
        assertTrue(result.contains(response2));
        verify(colorRepository).existsByName("Red");
        verify(colorRepository).existsByName("Blue");
        verify(colorRepository).saveAll(colors);
    }

    @Test
    void createMultipleColors_OneAlreadyExists() {
        // Given
        CreateColorRequest request2 = CreateColorRequest.builder()
                .name("Blue")
                .code("#0000FF")
                .build();

        List<CreateColorRequest> requests = Arrays.asList(createColorRequest, request2);

        when(colorRepository.existsByName("Red")).thenReturn(false);
        when(colorRepository.existsByName("Blue")).thenReturn(true);

        // When & Then
        assertThrows(ColorAlreadyExistsException.class, () -> colorService.createMultipleColors(requests));
        verify(colorRepository).existsByName("Red");
        verify(colorRepository).existsByName("Blue");
        verify(colorRepository, never()).saveAll(any());
    }

    @Test
    void createMultipleColors_EmptyList() {
        // Given
        List<CreateColorRequest> requests = Collections.emptyList();
        when(colorRepository.saveAll(Collections.emptyList())).thenReturn(Collections.emptyList());

        // When
        List<ColorResponse> result = colorService.createMultipleColors(requests);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(colorRepository).saveAll(Collections.emptyList());
    }

    @Test
    void createMultipleColors_DatabaseError() {
        // Given
        List<CreateColorRequest> requests = Arrays.asList(createColorRequest);
        List<Color> colors = Arrays.asList(color);

        when(colorRepository.existsByName("Red")).thenReturn(false);
        when(colorMapper.toEntity(createColorRequest)).thenReturn(color);
        when(colorRepository.saveAll(colors)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> colorService.createMultipleColors(requests));
        verify(colorRepository).saveAll(colors);
    }

    @Test
    void updateColor_Success() {
        // Given
        String id = "color-1";
        when(colorRepository.findById(id)).thenReturn(Optional.of(color));
        when(colorRepository.existsByName("Blue")).thenReturn(false);
        when(colorRepository.save(color)).thenReturn(color);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);

        // When
        ColorResponse result = colorService.updateColor(id, updateColorRequest);

        // Then
        assertNotNull(result);
        assertEquals(colorResponse, result);
        verify(colorRepository).findById(id);
        verify(colorRepository).existsByName("Blue");
        verify(colorMapper).updateEntity(updateColorRequest, color);
        verify(colorRepository).save(color);
        verify(colorMapper).toResponse(color);
    }

    @Test
    void updateColor_SameName() {
        // Given
        String id = "color-1";
        updateColorRequest.setName("Red"); // Same as existing name
        when(colorRepository.findById(id)).thenReturn(Optional.of(color));
        when(colorRepository.save(color)).thenReturn(color);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);

        // When
        ColorResponse result = colorService.updateColor(id, updateColorRequest);

        // Then
        assertNotNull(result);
        verify(colorRepository).findById(id);
        verify(colorRepository, never()).existsByName(anyString());
        verify(colorRepository).save(color);
    }

    @Test
    void updateColor_NotFound() {
        // Given
        String id = "non-existent";
        when(colorRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ColorNotFoundException.class, () -> colorService.updateColor(id, updateColorRequest));
        verify(colorRepository).findById(id);
        verify(colorRepository, never()).save(any());
    }

    @Test
    void updateColor_NewNameAlreadyExists() {
        // Given
        String id = "color-1";
        when(colorRepository.findById(id)).thenReturn(Optional.of(color));
        when(colorRepository.existsByName("Blue")).thenReturn(true);

        // When & Then
        assertThrows(ColorAlreadyExistsException.class, () -> colorService.updateColor(id, updateColorRequest));
        verify(colorRepository).findById(id);
        verify(colorRepository).existsByName("Blue");
        verify(colorRepository, never()).save(any());
    }

    @Test
    void updateColor_DatabaseError() {
        // Given
        String id = "color-1";
        when(colorRepository.findById(id)).thenReturn(Optional.of(color));
        when(colorRepository.existsByName("Blue")).thenReturn(false);
        when(colorRepository.save(color)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> colorService.updateColor(id, updateColorRequest));
        verify(colorRepository).save(color);
    }

    @Test
    void deleteColor_Success() {
        // Given
        String id = "color-1";
        when(colorRepository.existsById(id)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> colorService.deleteColor(id));

        // Then
        verify(colorRepository).existsById(id);
        verify(colorRepository).deleteById(id);
    }

    @Test
    void deleteColor_NotFound() {
        // Given
        String id = "non-existent";
        when(colorRepository.existsById(id)).thenReturn(false);

        // When & Then
        assertThrows(ColorNotFoundException.class, () -> colorService.deleteColor(id));
        verify(colorRepository).existsById(id);
        verify(colorRepository, never()).deleteById(id);
    }

    @Test
    void deleteColor_DatabaseError() {
        // Given
        String id = "color-1";
        when(colorRepository.existsById(id)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Database error")).when(colorRepository).deleteById(id);

        // When & Then
        assertThrows(DataAccessException.class, () -> colorService.deleteColor(id));
        verify(colorRepository).existsById(id);
        verify(colorRepository).deleteById(id);
    }

    @Test
    void deleteColor_UnexpectedError() {
        // Given
        String id = "color-1";
        when(colorRepository.existsById(id)).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(colorRepository).deleteById(id);

        // When & Then
        assertThrows(RuntimeException.class, () -> colorService.deleteColor(id));
        verify(colorRepository).existsById(id);
        verify(colorRepository).deleteById(id);
    }

    @Test
    void findColorById_Success() {
        // Given
        String id = "color-1";
        when(colorRepository.findById(id)).thenReturn(Optional.of(color));

        // When
        Color result = colorService.findColorById(id);

        // Then
        assertNotNull(result);
        assertEquals(color, result);
        verify(colorRepository).findById(id);
    }

    @Test
    void findColorById_NotFound() {
        // Given
        String id = "non-existent";
        when(colorRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ColorNotFoundException.class, () -> colorService.findColorById(id));
        verify(colorRepository).findById(id);
    }

    @Test
    void doesColorExistById_True() {
        // Given
        String id = "color-1";
        when(colorRepository.existsById(id)).thenReturn(true);

        // When
        boolean result = colorService.doesColorExistById(id);

        // Then
        assertTrue(result);
        verify(colorRepository).existsById(id);
    }

    @Test
    void doesColorExistById_False() {
        // Given
        String id = "non-existent";
        when(colorRepository.existsById(id)).thenReturn(false);

        // When
        boolean result = colorService.doesColorExistById(id);

        // Then
        assertFalse(result);
        verify(colorRepository).existsById(id);
    }

    @Test
    void checkColorNameExists_Exists() {
        // Given
        String name = "Red";
        when(colorRepository.existsByName(name)).thenReturn(true);

        // When & Then
        assertThrows(ColorAlreadyExistsException.class, () -> colorService.checkColorNameExists(name));
        verify(colorRepository).existsByName(name);
    }

    @Test
    void checkColorNameExists_NotExists() {
        // Given
        String name = "Red";
        when(colorRepository.existsByName(name)).thenReturn(false);

        // When
        assertDoesNotThrow(() -> colorService.checkColorNameExists(name));

        // Then
        verify(colorRepository).existsByName(name);
    }

    @Test
    void createMultipleColors_UnexpectedError() {
        // Given
        List<CreateColorRequest> requests = Arrays.asList(createColorRequest);
        when(colorRepository.existsByName("Red")).thenReturn(false);
        when(colorMapper.toEntity(createColorRequest)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> colorService.createMultipleColors(requests));
        verify(colorRepository).existsByName("Red");
        verify(colorMapper).toEntity(createColorRequest);
    }

    @Test
    void updateColor_UnexpectedError() {
        // Given
        String id = "color-1";
        when(colorRepository.findById(id)).thenReturn(Optional.of(color));
        when(colorRepository.existsByName("Blue")).thenReturn(false);
        doThrow(new RuntimeException("Mapping error")).when(colorMapper).updateEntity(updateColorRequest, color);

        // When & Then
        assertThrows(RuntimeException.class, () -> colorService.updateColor(id, updateColorRequest));
        verify(colorRepository).findById(id);
        verify(colorMapper).updateEntity(updateColorRequest, color);
    }

    @Test
    void createMultipleColors_SingleColor() {
        // Given
        List<CreateColorRequest> requests = Arrays.asList(createColorRequest);
        List<Color> colors = Arrays.asList(color);
        List<Color> savedColors = Arrays.asList(color);

        when(colorRepository.existsByName("Red")).thenReturn(false);
        when(colorMapper.toEntity(createColorRequest)).thenReturn(color);
        when(colorRepository.saveAll(colors)).thenReturn(savedColors);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);

        // When
        List<ColorResponse> result = colorService.createMultipleColors(requests);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(colorResponse, result.get(0));
        verify(colorRepository).saveAll(colors);
    }

    @Test
    void searchColors_NoResults() {
        // Given
        String keyword = "NonExistent";
        Page<Color> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(colorRepository.findByNameContainingIgnoreCase(keyword, pageable)).thenReturn(emptyPage);

        // When
        Page<ColorResponse> result = colorService.searchColors(pageable, keyword);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(colorRepository).findByNameContainingIgnoreCase(keyword, pageable);
    }
}
