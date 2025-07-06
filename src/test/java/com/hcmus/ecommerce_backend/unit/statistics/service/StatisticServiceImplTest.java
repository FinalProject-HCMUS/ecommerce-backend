package com.hcmus.ecommerce_backend.unit.statistics.service;

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
import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.order.repository.OrderRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductRepository;
import com.hcmus.ecommerce_backend.statistics.service.impl.StatisticServiceImpl;

@ExtendWith(MockitoExtension.class)
public class StatisticServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private StatisticServiceImpl statisticService;

    private Category category1;
    private Category category2;
    private List<Category> categories;

    @BeforeEach
    void setUp() {
        category1 = new Category();
        category1.setId("cat-1");
        category1.setName("Electronics");
        category1.setStock(100);

        category2 = new Category();
        category2.setId("cat-2");
        category2.setName("Clothing");
        category2.setStock(50);

        categories = Arrays.asList(category1, category2);
    }

    // getSalesAnalysis tests for "year" type
    @Test
    void getSalesAnalysis_YearType_Success() {
        // Given
        String type = "year";
        String date = "2024";
        int currentYear = 2024;
        int previousYear = 2023;

        // Mock monthly revenues for current year
        when(orderRepository.sumSubTotalByMonthAndYear(1, currentYear)).thenReturn(1000.0);
        when(orderRepository.sumSubTotalByMonthAndYear(2, currentYear)).thenReturn(1500.0);
        when(orderRepository.sumSubTotalByMonthAndYear(3, currentYear)).thenReturn(null); // Test null handling
        when(orderRepository.sumSubTotalByMonthAndYear(4, currentYear)).thenReturn(2000.0);
        when(orderRepository.sumSubTotalByMonthAndYear(5, currentYear)).thenReturn(1800.0);
        when(orderRepository.sumSubTotalByMonthAndYear(6, currentYear)).thenReturn(2200.0);
        when(orderRepository.sumSubTotalByMonthAndYear(7, currentYear)).thenReturn(2500.0);
        when(orderRepository.sumSubTotalByMonthAndYear(8, currentYear)).thenReturn(2300.0);
        when(orderRepository.sumSubTotalByMonthAndYear(9, currentYear)).thenReturn(2100.0);
        when(orderRepository.sumSubTotalByMonthAndYear(10, currentYear)).thenReturn(1900.0);
        when(orderRepository.sumSubTotalByMonthAndYear(11, currentYear)).thenReturn(2400.0);
        when(orderRepository.sumSubTotalByMonthAndYear(12, currentYear)).thenReturn(2600.0);

        // Mock yearly totals
        when(orderRepository.sumSubTotalByYear(currentYear)).thenReturn(20000.0);
        when(orderRepository.sumSubTotalByYear(previousYear)).thenReturn(18000.0);
        when(productRepository.sumPriceQuantityByYear(currentYear)).thenReturn(15000.0);
        when(productRepository.sumPriceQuantityByYear(previousYear)).thenReturn(14000.0);

        // When
        Map<String, Object> result = statisticService.getSalesAnalysis(type, date);

        // Then
        assertNotNull(result);
        assertEquals(5, result.size());

        @SuppressWarnings("unchecked")
        List<Integer> labels = (List<Integer>) result.get("labels");
        assertEquals(12, labels.size());
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), labels);

        @SuppressWarnings("unchecked")
        List<String> revenues = (List<String>) result.get("revenues");
        assertEquals(12, revenues.size());
        assertEquals("1000.00", revenues.get(0));
        assertEquals("1500.00", revenues.get(1));
        assertEquals("0.00", revenues.get(2)); // null converted to 0.0

        @SuppressWarnings("unchecked")
        List<String> totalIncome = (List<String>) result.get("totalIncome");
        assertEquals(2, totalIncome.size());
        assertEquals("20000.00", totalIncome.get(0));
        assertEquals("11.11", totalIncome.get(1)); // (20000-18000)/18000*100

        @SuppressWarnings("unchecked")
        List<String> totalExpense = (List<String>) result.get("totalExpense");
        assertEquals(2, totalExpense.size());
        assertEquals("15000.00", totalExpense.get(0));

        @SuppressWarnings("unchecked")
        List<String> totalBalance = (List<String>) result.get("totalBalance");
        assertEquals(2, totalBalance.size());
        assertEquals("5000.00", totalBalance.get(0)); // 20000 - 15000

        verify(orderRepository, times(12)).sumSubTotalByMonthAndYear(anyInt(), eq(currentYear));
        verify(orderRepository).sumSubTotalByYear(currentYear);
        verify(orderRepository).sumSubTotalByYear(previousYear);
        verify(productRepository).sumPriceQuantityByYear(currentYear);
        verify(productRepository).sumPriceQuantityByYear(previousYear);
    }

    @Test
    void getSalesAnalysis_YearType_WithNullTotals() {
        // Given
        String type = "year";
        String date = "2024";
        int currentYear = 2024;
        int previousYear = 2023;

        // Mock all monthly revenues as null
        for (int month = 1; month <= 12; month++) {
            when(orderRepository.sumSubTotalByMonthAndYear(month, currentYear)).thenReturn(null);
        }

        // Mock yearly totals as null
        when(orderRepository.sumSubTotalByYear(currentYear)).thenReturn(null);
        when(orderRepository.sumSubTotalByYear(previousYear)).thenReturn(null);
        when(productRepository.sumPriceQuantityByYear(currentYear)).thenReturn(null);
        when(productRepository.sumPriceQuantityByYear(previousYear)).thenReturn(null);

        // When
        Map<String, Object> result = statisticService.getSalesAnalysis(type, date);

        // Then
        assertNotNull(result);

        @SuppressWarnings("unchecked")
        List<String> revenues = (List<String>) result.get("revenues");
        assertEquals(12, revenues.size());
        assertTrue(revenues.stream().allMatch(r -> r.equals("0.00")));

        @SuppressWarnings("unchecked")
        List<String> totalIncome = (List<String>) result.get("totalIncome");
        assertEquals("0.00", totalIncome.get(0));
        assertEquals("0.00", totalIncome.get(1)); // 0% change when both are 0
    }

    // getSalesAnalysis tests for "month" type
    @Test
    void getSalesAnalysis_MonthType_Success() {
        // Given
        String type = "month";
        String date = "10-2024";
        int currentMonth = 10;
        int currentYear = 2024;
        int previousMonth = 9;
        int previousYear = 2024;

        // Mock daily revenues for October 2024 (31 days)
        for (int day = 1; day <= 31; day++) {
            when(orderRepository.sumSubTotalByDayAndMonth(day, date)).thenReturn(100.0 * day);
        }

        // Mock monthly totals
        when(orderRepository.sumSubTotalByMonthAndYear(currentMonth, currentYear)).thenReturn(5000.0);
        when(orderRepository.sumSubTotalByMonthAndYear(previousMonth, previousYear)).thenReturn(4500.0);
        when(productRepository.sumPriceQuantityByMonthAndYear(currentMonth, currentYear)).thenReturn(3000.0);
        when(productRepository.sumPriceQuantityByMonthAndYear(previousMonth, previousYear)).thenReturn(2800.0);

        // When
        Map<String, Object> result = statisticService.getSalesAnalysis(type, date);

        // Then
        assertNotNull(result);

        @SuppressWarnings("unchecked")
        List<Integer> labels = (List<Integer>) result.get("labels");
        assertEquals(31, labels.size());
        assertEquals(Integer.valueOf(1), labels.get(0));
        assertEquals(Integer.valueOf(31), labels.get(30));

        @SuppressWarnings("unchecked")
        List<String> revenues = (List<String>) result.get("revenues");
        assertEquals(31, revenues.size());
        assertEquals("100.00", revenues.get(0)); // day 1
        assertEquals("3100.00", revenues.get(30)); // day 31

        @SuppressWarnings("unchecked")
        List<String> totalIncome = (List<String>) result.get("totalIncome");
        assertEquals("5000.00", totalIncome.get(0));
        assertEquals("11.11", totalIncome.get(1)); // (5000-4500)/4500*100

        verify(orderRepository, times(31)).sumSubTotalByDayAndMonth(anyInt(), eq(date));
        verify(orderRepository).sumSubTotalByMonthAndYear(currentMonth, currentYear);
        verify(orderRepository).sumSubTotalByMonthAndYear(previousMonth, previousYear);
    }

    @Test
    void getSalesAnalysis_MonthType_JanuaryToPreviousYear() {
        // Given
        String type = "month";
        String date = "01-2024";
        int currentMonth = 1;
        int currentYear = 2024;
        int previousMonth = 12;
        int previousYear = 2023; // Previous year for January

        // Mock daily revenues for January 2024 (31 days)
        for (int day = 1; day <= 31; day++) {
            when(orderRepository.sumSubTotalByDayAndMonth(day, date)).thenReturn(50.0);
        }

        // Mock monthly totals
        when(orderRepository.sumSubTotalByMonthAndYear(currentMonth, currentYear)).thenReturn(3000.0);
        when(orderRepository.sumSubTotalByMonthAndYear(previousMonth, previousYear)).thenReturn(3500.0);
        when(productRepository.sumPriceQuantityByMonthAndYear(currentMonth, currentYear)).thenReturn(2000.0);
        when(productRepository.sumPriceQuantityByMonthAndYear(previousMonth, previousYear)).thenReturn(2300.0);

        // When
        Map<String, Object> result = statisticService.getSalesAnalysis(type, date);

        // Then
        assertNotNull(result);

        @SuppressWarnings("unchecked")
        List<String> totalIncome = (List<String>) result.get("totalIncome");
        assertEquals("3000.00", totalIncome.get(0));
        assertEquals("-14.29", totalIncome.get(1)); // (3000-3500)/3500*100 = negative change

        verify(orderRepository).sumSubTotalByMonthAndYear(previousMonth, previousYear);
        verify(productRepository).sumPriceQuantityByMonthAndYear(previousMonth, previousYear);
    }

    @Test
    void getSalesAnalysis_MonthType_FebruaryLeapYear() {
        // Given
        String type = "month";
        String date = "02-2024"; // 2024 is a leap year, February has 29 days

        // Mock daily revenues for February 2024 (29 days)
        for (int day = 1; day <= 29; day++) {
            when(orderRepository.sumSubTotalByDayAndMonth(day, date)).thenReturn(100.0);
        }

        when(orderRepository.sumSubTotalByMonthAndYear(2, 2024)).thenReturn(2900.0);
        when(orderRepository.sumSubTotalByMonthAndYear(1, 2024)).thenReturn(3100.0);
        when(productRepository.sumPriceQuantityByMonthAndYear(2, 2024)).thenReturn(2000.0);
        when(productRepository.sumPriceQuantityByMonthAndYear(1, 2024)).thenReturn(2100.0);

        // When
        Map<String, Object> result = statisticService.getSalesAnalysis(type, date);

        // Then
        assertNotNull(result);

        @SuppressWarnings("unchecked")
        List<Integer> labels = (List<Integer>) result.get("labels");
        assertEquals(29, labels.size()); // February 2024 has 29 days

        verify(orderRepository, times(29)).sumSubTotalByDayAndMonth(anyInt(), eq(date));
    }

    @Test
    void getSalesAnalysis_MonthType_FebruaryNonLeapYear() {
        // Given
        String type = "month";
        String date = "02-2023"; // 2023 is not a leap year, February has 28 days

        // Mock daily revenues for February 2023 (28 days)
        for (int day = 1; day <= 28; day++) {
            when(orderRepository.sumSubTotalByDayAndMonth(day, date)).thenReturn(100.0);
        }

        when(orderRepository.sumSubTotalByMonthAndYear(2, 2023)).thenReturn(2800.0);
        when(orderRepository.sumSubTotalByMonthAndYear(1, 2023)).thenReturn(3100.0);
        when(productRepository.sumPriceQuantityByMonthAndYear(2, 2023)).thenReturn(2000.0);
        when(productRepository.sumPriceQuantityByMonthAndYear(1, 2023)).thenReturn(2100.0);

        // When
        Map<String, Object> result = statisticService.getSalesAnalysis(type, date);

        // Then
        assertNotNull(result);

        @SuppressWarnings("unchecked")
        List<Integer> labels = (List<Integer>) result.get("labels");
        assertEquals(28, labels.size()); // February 2023 has 28 days

        verify(orderRepository, times(28)).sumSubTotalByDayAndMonth(anyInt(), eq(date));
    }

    @Test
    void getSalesAnalysis_InvalidType_ThrowsException() {
        // Given
        String type = "invalid";
        String date = "2024";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> statisticService.getSalesAnalysis(type, date));
        assertEquals("Invalid type. Allowed values are 'month' or 'year'.", exception.getMessage());
    }

    @Test
    void getSalesAnalysis_YearType_DatabaseError_ThrowsException() {
        // Given
        String type = "year";
        String date = "2024";

        when(orderRepository.sumSubTotalByMonthAndYear(anyInt(), anyInt()))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class,
                () -> statisticService.getSalesAnalysis(type, date));
    }

    @Test
    void getSalesAnalysis_MonthType_InvalidDateFormat_ThrowsException() {
        // Given
        String type = "month";
        String date = "invalid-date";

        // When & Then
        assertThrows(NumberFormatException.class,
                () -> statisticService.getSalesAnalysis(type, date));
    }

    @Test
    void getSalesAnalysis_YearType_InvalidYear_ThrowsException() {
        // Given
        String type = "year";
        String date = "invalid-year";

        // When & Then
        assertThrows(NumberFormatException.class,
                () -> statisticService.getSalesAnalysis(type, date));
    }

    @Test
    void getSalesAnalysis_YearType_CaseInsensitive() {
        // Given
        String type = "YEAR";
        String date = "2024";

        when(orderRepository.sumSubTotalByMonthAndYear(anyInt(), anyInt())).thenReturn(100.0);
        when(orderRepository.sumSubTotalByYear(anyInt())).thenReturn(1200.0);
        when(productRepository.sumPriceQuantityByYear(anyInt())).thenReturn(800.0);

        // When
        Map<String, Object> result = statisticService.getSalesAnalysis(type, date);

        // Then
        assertNotNull(result);
        verify(orderRepository, times(12)).sumSubTotalByMonthAndYear(anyInt(), eq(2024));
    }

    @Test
    void getSalesAnalysis_MonthType_CaseInsensitive() {
        // Given
        String type = "MONTH";
        String date = "10-2024";

        for (int day = 1; day <= 31; day++) {
            when(orderRepository.sumSubTotalByDayAndMonth(day, date)).thenReturn(100.0);
        }
        when(orderRepository.sumSubTotalByMonthAndYear(anyInt(), anyInt())).thenReturn(1000.0);
        when(productRepository.sumPriceQuantityByMonthAndYear(anyInt(), anyInt())).thenReturn(700.0);

        // When
        Map<String, Object> result = statisticService.getSalesAnalysis(type, date);

        // Then
        assertNotNull(result);
        verify(orderRepository, times(31)).sumSubTotalByDayAndMonth(anyInt(), eq(date));
    }

    // getBestSellers tests
    @Test
    void getBestSellers_Success() {
        // Given
        String type = "year";
        String date = "2024";

        Map<String, Object> product1 = Map.of(
                "id", "prod-1",
                "name", "Product 1",
                "totalSold", 100);
        Map<String, Object> product2 = Map.of(
                "id", "prod-2",
                "name", "Product 2",
                "totalSold", 80);
        List<Map<String, Object>> bestSellers = Arrays.asList(product1, product2);

        when(productRepository.findBestSellersByTypeAndDate(type, date)).thenReturn(bestSellers);

        // When
        Map<String, Object> result = statisticService.getBestSellers(type, date);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("products"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> products = (List<Map<String, Object>>) result.get("products");
        assertEquals(2, products.size());
        assertEquals(bestSellers, products);

        verify(productRepository).findBestSellersByTypeAndDate(type, date);
    }

    @Test
    void getBestSellers_EmptyResult() {
        // Given
        String type = "month";
        String date = "10-2024";

        when(productRepository.findBestSellersByTypeAndDate(type, date)).thenReturn(Collections.emptyList());

        // When
        Map<String, Object> result = statisticService.getBestSellers(type, date);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("products"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> products = (List<Map<String, Object>>) result.get("products");
        assertTrue(products.isEmpty());

        verify(productRepository).findBestSellersByTypeAndDate(type, date);
    }

    @Test
    void getBestSellers_DatabaseError_ThrowsException() {
        // Given
        String type = "year";
        String date = "2024";

        when(productRepository.findBestSellersByTypeAndDate(type, date))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class,
                () -> statisticService.getBestSellers(type, date));
        verify(productRepository).findBestSellersByTypeAndDate(type, date);
    }

    @Test
    void getBestSellers_UnexpectedError_ThrowsException() {
        // Given
        String type = "year";
        String date = "2024";

        when(productRepository.findBestSellersByTypeAndDate(type, date))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> statisticService.getBestSellers(type, date));
        verify(productRepository).findBestSellersByTypeAndDate(type, date);
    }

    // getProductCategoryStatistics tests
    @Test
    void getProductCategoryStatistics_Success() {
        // Given
        when(categoryRepository.findAll()).thenReturn(categories);

        // When
        Map<String, Object> result = statisticService.getProductCategoryStatistics();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("categories"));
        assertTrue(result.containsKey("data"));

        @SuppressWarnings("unchecked")
        List<String> categoryNames = (List<String>) result.get("categories");
        assertEquals(Arrays.asList("Electronics", "Clothing"), categoryNames);

        @SuppressWarnings("unchecked")
        List<Integer> categoryStocks = (List<Integer>) result.get("data");
        assertEquals(Arrays.asList(100, 50), categoryStocks);

        verify(categoryRepository).findAll();
    }

    @Test
    void getProductCategoryStatistics_EmptyCategories() {
        // Given
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        Map<String, Object> result = statisticService.getProductCategoryStatistics();

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("categories"));
        assertTrue(result.containsKey("data"));

        @SuppressWarnings("unchecked")
        List<String> categoryNames = (List<String>) result.get("categories");
        assertTrue(categoryNames.isEmpty());

        @SuppressWarnings("unchecked")
        List<Integer> categoryStocks = (List<Integer>) result.get("data");
        assertTrue(categoryStocks.isEmpty());

        verify(categoryRepository).findAll();
    }

    @Test
    void getProductCategoryStatistics_DatabaseError_ThrowsException() {
        // Given
        when(categoryRepository.findAll()).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class,
                () -> statisticService.getProductCategoryStatistics());
        verify(categoryRepository).findAll();
    }

    @Test
    void getProductCategoryStatistics_UnexpectedError_ThrowsException() {
        // Given
        when(categoryRepository.findAll()).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> statisticService.getProductCategoryStatistics());
        verify(categoryRepository).findAll();
    }

    @Test
    void getProductCategoryStatistics_SingleCategory() {
        // Given
        List<Category> singleCategory = Arrays.asList(category1);
        when(categoryRepository.findAll()).thenReturn(singleCategory);

        // When
        Map<String, Object> result = statisticService.getProductCategoryStatistics();

        // Then
        assertNotNull(result);

        @SuppressWarnings("unchecked")
        List<String> categoryNames = (List<String>) result.get("categories");
        assertEquals(1, categoryNames.size());
        assertEquals("Electronics", categoryNames.get(0));

        @SuppressWarnings("unchecked")
        List<Integer> categoryStocks = (List<Integer>) result.get("data");
        assertEquals(1, categoryStocks.size());
        assertEquals(Integer.valueOf(100), categoryStocks.get(0));

        verify(categoryRepository).findAll();
    }

    // getIncompleteOrders tests
    @Test
    void getIncompleteOrders_Success() {
        // Given
        String date = "10-2024";

        Map<String, Object> order1 = Map.of(
                "id", "order-1",
                "status", "PENDING",
                "totalAmount", 1000.0);
        Map<String, Object> order2 = Map.of(
                "id", "order-2",
                "status", "PROCESSING",
                "totalAmount", 1500.0);
        List<Map<String, Object>> incompleteOrders = Arrays.asList(order1, order2);

        when(orderRepository.findIncompleteOrders(date)).thenReturn(incompleteOrders);

        // When
        Map<String, Object> result = statisticService.getIncompleteOrders(date);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("orders"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orders = (List<Map<String, Object>>) result.get("orders");
        assertEquals(2, orders.size());
        assertEquals(incompleteOrders, orders);

        verify(orderRepository).findIncompleteOrders(date);
    }

    @Test
    void getIncompleteOrders_EmptyResult() {
        // Given
        String date = "10-2024";

        when(orderRepository.findIncompleteOrders(date)).thenReturn(Collections.emptyList());

        // When
        Map<String, Object> result = statisticService.getIncompleteOrders(date);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("orders"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orders = (List<Map<String, Object>>) result.get("orders");
        assertTrue(orders.isEmpty());

        verify(orderRepository).findIncompleteOrders(date);
    }

    @Test
    void getIncompleteOrders_NullDate() {
        // Given
        String date = null;

        Map<String, Object> order1 = Map.of("id", "order-1", "status", "PENDING");
        List<Map<String, Object>> incompleteOrders = Arrays.asList(order1);

        when(orderRepository.findIncompleteOrders(date)).thenReturn(incompleteOrders);

        // When
        Map<String, Object> result = statisticService.getIncompleteOrders(date);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("orders"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orders = (List<Map<String, Object>>) result.get("orders");
        assertEquals(1, orders.size());

        verify(orderRepository).findIncompleteOrders(date);
    }

    @Test
    void getIncompleteOrders_DatabaseError_ThrowsException() {
        // Given
        String date = "10-2024";

        when(orderRepository.findIncompleteOrders(date))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class,
                () -> statisticService.getIncompleteOrders(date));
        verify(orderRepository).findIncompleteOrders(date);
    }

    @Test
    void getIncompleteOrders_UnexpectedError_ThrowsException() {
        // Given
        String date = "10-2024";

        when(orderRepository.findIncompleteOrders(date))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> statisticService.getIncompleteOrders(date));
        verify(orderRepository).findIncompleteOrders(date);
    }

    // Test percentage calculation edge cases
    @Test
    void getSalesAnalysis_YearType_ZeroPreviousValues() {
        // Given
        String type = "year";
        String date = "2024";

        for (int month = 1; month <= 12; month++) {
            when(orderRepository.sumSubTotalByMonthAndYear(month, 2024)).thenReturn(100.0);
        }

        when(orderRepository.sumSubTotalByYear(2024)).thenReturn(1000.0);
        when(orderRepository.sumSubTotalByYear(2023)).thenReturn(0.0); // Previous year is 0
        when(productRepository.sumPriceQuantityByYear(2024)).thenReturn(800.0);
        when(productRepository.sumPriceQuantityByYear(2023)).thenReturn(0.0); // Previous year is 0

        // When
        Map<String, Object> result = statisticService.getSalesAnalysis(type, date);

        // Then
        @SuppressWarnings("unchecked")
        List<String> totalIncome = (List<String>) result.get("totalIncome");
        assertEquals("100.00", totalIncome.get(1)); // 100% increase when previous is 0

        @SuppressWarnings("unchecked")
        List<String> totalExpense = (List<String>) result.get("totalExpense");
        assertEquals("100.00", totalExpense.get(1)); // 100% increase when previous is 0
    }

    @Test
    void getSalesAnalysis_YearType_ZeroCurrentAndPreviousValues() {
        // Given
        String type = "year";
        String date = "2024";

        for (int month = 1; month <= 12; month++) {
            when(orderRepository.sumSubTotalByMonthAndYear(month, 2024)).thenReturn(0.0);
        }

        when(orderRepository.sumSubTotalByYear(2024)).thenReturn(0.0);
        when(orderRepository.sumSubTotalByYear(2023)).thenReturn(0.0);
        when(productRepository.sumPriceQuantityByYear(2024)).thenReturn(0.0);
        when(productRepository.sumPriceQuantityByYear(2023)).thenReturn(0.0);

        // When
        Map<String, Object> result = statisticService.getSalesAnalysis(type, date);

        // Then
        @SuppressWarnings("unchecked")
        List<String> totalIncome = (List<String>) result.get("totalIncome");
        assertEquals("0.00", totalIncome.get(1)); // 0% change when both are 0

        @SuppressWarnings("unchecked")
        List<String> totalExpense = (List<String>) result.get("totalExpense");
        assertEquals("0.00", totalExpense.get(1)); // 0% change when both are 0

        @SuppressWarnings("unchecked")
        List<String> totalBalance = (List<String>) result.get("totalBalance");
        assertEquals("0.00", totalBalance.get(1)); // 0% change when both are 0
    }

    @Test
    void getSalesAnalysis_MonthType_NegativeBalance() {
        // Given
        String type = "month";
        String date = "10-2024";

        for (int day = 1; day <= 31; day++) {
            when(orderRepository.sumSubTotalByDayAndMonth(day, date)).thenReturn(50.0);
        }

        // Expenses higher than income
        when(orderRepository.sumSubTotalByMonthAndYear(10, 2024)).thenReturn(1000.0);
        when(orderRepository.sumSubTotalByMonthAndYear(9, 2024)).thenReturn(1200.0);
        when(productRepository.sumPriceQuantityByMonthAndYear(10, 2024)).thenReturn(1500.0); // Higher expense
        when(productRepository.sumPriceQuantityByMonthAndYear(9, 2024)).thenReturn(1100.0);

        // When
        Map<String, Object> result = statisticService.getSalesAnalysis(type, date);

        // Then
        @SuppressWarnings("unchecked")
        List<String> totalBalance = (List<String>) result.get("totalBalance");
        assertEquals("-500.00", totalBalance.get(0)); // 1000 - 1500 = -500 (negative balance)

        // Calculate expected percentage change:
        // previous balance = 1200 - 1100 = 100
        // current balance = 1000 - 1500 = -500
        // percentage change = ((-500 - 100) / 100) * 100 = -600%
        assertEquals("-600.00", totalBalance.get(1)); // Correct percentage change calculation
    }

    @Test
    void getSalesAnalysis_TypeEmpty_ThrowsException() {
        // Given
        String type = "";
        String date = "2024";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> statisticService.getSalesAnalysis(type, date));
        assertEquals("Type parameter cannot be empty", exception.getMessage());
    }

    // Alternative test for null date with month type
    @Test
    void getSalesAnalysis_MonthType_NullDate_ThrowsException() {
        // Given
        String type = "month";
        String date = null;

        // When & Then
        // Will throw NullPointerException when trying to split null string
        assertThrows(IllegalArgumentException.class,
                () -> statisticService.getSalesAnalysis(type, date));
    }

    // Test for malformed date in month type
    @Test
    void getSalesAnalysis_MonthType_MalformedDate_ThrowsException() {
        // Given
        String type = "month";
        String date = "10"; // Missing year part

        // When & Then
        // Will throw ArrayIndexOutOfBoundsException when trying to access parts[1]
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> statisticService.getSalesAnalysis(type, date));
    }

    // Test for invalid month format
    @Test
    void getSalesAnalysis_MonthType_InvalidMonthFormat_ThrowsException() {
        // Given
        String type = "month";
        String date = "invalid-2024";

        // When & Then
        assertThrows(NumberFormatException.class,
                () -> statisticService.getSalesAnalysis(type, date));
    }

    // Additional test for edge case: previous value is negative
    @Test
    void getSalesAnalysis_YearType_NegativePreviousValue() {
        // Given
        String type = "year";
        String date = "2024";

        for (int month = 1; month <= 12; month++) {
            when(orderRepository.sumSubTotalByMonthAndYear(month, 2024)).thenReturn(100.0);
        }

        when(orderRepository.sumSubTotalByYear(2024)).thenReturn(1000.0);
        when(orderRepository.sumSubTotalByYear(2023)).thenReturn(800.0);
        when(productRepository.sumPriceQuantityByYear(2024)).thenReturn(500.0);
        when(productRepository.sumPriceQuantityByYear(2023)).thenReturn(900.0); // Previous expense higher than income

        // When
        Map<String, Object> result = statisticService.getSalesAnalysis(type, date);

        // Then
        @SuppressWarnings("unchecked")
        List<String> totalBalance = (List<String>) result.get("totalBalance");
        assertEquals("500.00", totalBalance.get(0)); // 1000 - 500 = 500 (current balance)

        // Previous balance = 800 - 900 = -100
        // Current balance = 1000 - 500 = 500
        // Percentage change = ((500 - (-100)) / (-100)) * 100 = -600%
        assertEquals("-600.00", totalBalance.get(1));
    }

    // Test for zero division edge case in percentage calculation
    @Test
    void getSalesAnalysis_YearType_ZeroDivision_HandledCorrectly() {
        // Given
        String type = "year";
        String date = "2024";

        for (int month = 1; month <= 12; month++) {
            when(orderRepository.sumSubTotalByMonthAndYear(month, 2024)).thenReturn(50.0);
        }

        // Current year has values, previous year has zero
        when(orderRepository.sumSubTotalByYear(2024)).thenReturn(600.0);
        when(orderRepository.sumSubTotalByYear(2023)).thenReturn(0.0);
        when(productRepository.sumPriceQuantityByYear(2024)).thenReturn(400.0);
        when(productRepository.sumPriceQuantityByYear(2023)).thenReturn(0.0);

        // When
        Map<String, Object> result = statisticService.getSalesAnalysis(type, date);

        // Then
        @SuppressWarnings("unchecked")
        List<String> totalIncome = (List<String>) result.get("totalIncome");
        assertEquals("600.00", totalIncome.get(0));
        assertEquals("100.00", totalIncome.get(1)); // 100% when previous is 0

        @SuppressWarnings("unchecked")
        List<String> totalExpense = (List<String>) result.get("totalExpense");
        assertEquals("400.00", totalExpense.get(0));
        assertEquals("100.00", totalExpense.get(1)); // 100% when previous is 0

        @SuppressWarnings("unchecked")
        List<String> totalBalance = (List<String>) result.get("totalBalance");
        assertEquals("200.00", totalBalance.get(0)); // 600 - 400 = 200
        assertEquals("100.00", totalBalance.get(1)); // 100% when previous is 0
    }

    @Test
    void getSalesAnalysis_MonthType_WithNullDailyRevenues() {
        // Given
        String type = "month";
        String date = "10-2024";

        // Some days have null revenues
        for (int day = 1; day <= 15; day++) {
            when(orderRepository.sumSubTotalByDayAndMonth(day, date)).thenReturn(100.0);
        }
        for (int day = 16; day <= 31; day++) {
            when(orderRepository.sumSubTotalByDayAndMonth(day, date)).thenReturn(null);
        }

        when(orderRepository.sumSubTotalByMonthAndYear(10, 2024)).thenReturn(1500.0);
        when(orderRepository.sumSubTotalByMonthAndYear(9, 2024)).thenReturn(1200.0);
        when(productRepository.sumPriceQuantityByMonthAndYear(10, 2024)).thenReturn(1000.0);
        when(productRepository.sumPriceQuantityByMonthAndYear(9, 2024)).thenReturn(800.0);

        // When
        Map<String, Object> result = statisticService.getSalesAnalysis(type, date);

        // Then
        @SuppressWarnings("unchecked")
        List<String> revenues = (List<String>) result.get("revenues");
        assertEquals(31, revenues.size());
        assertEquals("100.00", revenues.get(0)); // Day 1
        assertEquals("0.00", revenues.get(15)); // Day 16 (null converted to 0)
        assertEquals("0.00", revenues.get(30)); // Day 31 (null converted to 0)
    }

    @Test
    void getSalesAnalysis_TypeNull_ThrowsException() {
        // Given
        String type = null;
        String date = "2024";
        assertThrows(IllegalArgumentException.class,
                () -> statisticService.getSalesAnalysis(type, date));
    }

    @Test
    void getSalesAnalysis_DateNull_ThrowsException() {
        // Given
        String type = "year";
        String date = null;

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> statisticService.getSalesAnalysis(type, date));
    }

    // Test edge case months
    @Test
    void getSalesAnalysis_MonthType_April30Days() {
        // Given
        String type = "month";
        String date = "04-2024"; // April has 30 days

        for (int day = 1; day <= 30; day++) {
            when(orderRepository.sumSubTotalByDayAndMonth(day, date)).thenReturn(100.0);
        }

        when(orderRepository.sumSubTotalByMonthAndYear(4, 2024)).thenReturn(3000.0);
        when(orderRepository.sumSubTotalByMonthAndYear(3, 2024)).thenReturn(3100.0);
        when(productRepository.sumPriceQuantityByMonthAndYear(4, 2024)).thenReturn(2000.0);
        when(productRepository.sumPriceQuantityByMonthAndYear(3, 2024)).thenReturn(2100.0);

        // When
        Map<String, Object> result = statisticService.getSalesAnalysis(type, date);

        // Then
        @SuppressWarnings("unchecked")
        List<Integer> labels = (List<Integer>) result.get("labels");
        assertEquals(30, labels.size()); // April has 30 days

        verify(orderRepository, times(30)).sumSubTotalByDayAndMonth(anyInt(), eq(date));
    }

    @Test
    void getBestSellers_NullParameters() {
        // Given
        String type = null;
        String date = null;

        when(productRepository.findBestSellersByTypeAndDate(type, date)).thenReturn(Collections.emptyList());

        // When
        Map<String, Object> result = statisticService.getBestSellers(type, date);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("products"));
        verify(productRepository).findBestSellersByTypeAndDate(type, date);
    }

    @Test
    void getIncompleteOrders_EmptyDate() {
        // Given
        String date = "";

        when(orderRepository.findIncompleteOrders(date)).thenReturn(Collections.emptyList());

        // When
        Map<String, Object> result = statisticService.getIncompleteOrders(date);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("orders"));
        verify(orderRepository).findIncompleteOrders(date);
    }
}
