package com.hcmus.ecommerce_backend.statistics.service.impl;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.order.repository.OrderRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductRepository;
import com.hcmus.ecommerce_backend.statistics.service.StatisticService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {
    
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    public Map<String, Object> getSalesAnalysis(String type, String date) {
        log.info("StatisticServiceImpl | getSalesAnalysis | type: {}, date: {}", type, date);

        // Khởi tạo các danh sách kết quả
        List<Integer> labels = new ArrayList<>();
        List<Double> revenues = new ArrayList<>();
        List<Double> totalIncome = new ArrayList<>();
        List<Double> totalExpense = new ArrayList<>();
        List<Double> totalBalance = new ArrayList<>();

        if ("year".equalsIgnoreCase(type)) {
            // Lấy năm hiện tại và năm trước
            int currentYear = Integer.parseInt(date);
            int previousYear = currentYear - 1;

            // Hiển thị 12 tháng
            for (int month = 1; month <= 12; month++) {
                labels.add(month);

                // Tính toán doanh thu (Sub_total của order)
                Double income = orderRepository.sumSubTotalByMonthAndYear(month, currentYear);
                if (income == null) income = 0.0;
                revenues.add(income);
            }

            // Tính tổng của năm hiện tại
            Double totalIncomeCurrentYear = orderRepository.sumSubTotalByYear(currentYear);
            Double totalExpenseCurrentYear = productRepository.sumPriceQuantityByYear(currentYear);
            if (totalIncomeCurrentYear == null) totalIncomeCurrentYear = 0.0;
            if (totalExpenseCurrentYear == null) totalExpenseCurrentYear = 0.0;
            Double totalBalanceCurrentYear = totalIncomeCurrentYear - totalExpenseCurrentYear;

            // Tính tổng của năm trước
            Double totalIncomePreviousYear = orderRepository.sumSubTotalByYear(previousYear);
            Double totalExpensePreviousYear = productRepository.sumPriceQuantityByYear(previousYear);
            if (totalIncomePreviousYear == null) totalIncomePreviousYear = 0.0;
            if (totalExpensePreviousYear == null) totalExpensePreviousYear = 0.0;
            Double totalBalancePreviousYear = totalIncomePreviousYear - totalExpensePreviousYear;

            // Tính % thay đổi so với năm trước
            Double incomeChange = calculatePercentageChange(totalIncomePreviousYear, totalIncomeCurrentYear);
            Double expenseChange = calculatePercentageChange(totalExpensePreviousYear, totalExpenseCurrentYear);
            Double balanceChange = calculatePercentageChange(totalBalancePreviousYear, totalBalanceCurrentYear);

            // Thêm vào kết quả
            totalIncome.add(totalIncomeCurrentYear);
            totalIncome.add(incomeChange);

            totalExpense.add(totalExpenseCurrentYear);
            totalExpense.add(expenseChange);

            totalBalance.add(totalBalanceCurrentYear);
            totalBalance.add(balanceChange);

        } else if ("month".equalsIgnoreCase(type)) {
            // Lấy tháng và năm từ chuỗi date
            String[] parts = date.split("-");
            int currentMonth = Integer.parseInt(parts[0]);
            int currentYear = Integer.parseInt(parts[1]);

            // Tính tháng và năm trước đó
            int previousMonth = currentMonth == 1 ? 12 : currentMonth - 1;
            int previousYear = currentMonth == 1 ? currentYear - 1 : currentYear;

            // Lấy số ngày trong tháng hiện tại
            int daysInMonth = getDaysInMonth(date);

            // Hiển thị từng ngày trong tháng hiện tại
            for (int day = 1; day <= daysInMonth; day++) {
                labels.add(day);

                // Tính toán doanh thu (Sub_total của order) cho từng ngày
                Double dailyIncome = orderRepository.sumSubTotalByDayAndMonth(day, date);
                if (dailyIncome == null) dailyIncome = 0.0;
                revenues.add(dailyIncome);
            }

            // Tính tổng của tháng hiện tại
            Double totalIncomeCurrentMonth = orderRepository.sumSubTotalByMonthAndYear(currentMonth, currentYear);
            Double totalExpenseCurrentMonth = productRepository.sumPriceQuantityByMonthAndYear(currentMonth, currentYear);
            if (totalIncomeCurrentMonth == null) totalIncomeCurrentMonth = 0.0;
            if (totalExpenseCurrentMonth == null) totalExpenseCurrentMonth = 0.0;
            Double totalBalanceCurrentMonth = totalIncomeCurrentMonth - totalExpenseCurrentMonth;

            // Tính tổng của tháng trước
            Double totalIncomePreviousMonth = orderRepository.sumSubTotalByMonthAndYear(previousMonth, previousYear);
            Double totalExpensePreviousMonth = productRepository.sumPriceQuantityByMonthAndYear(previousMonth, previousYear);
            if (totalIncomePreviousMonth == null) totalIncomePreviousMonth = 0.0;
            if (totalExpensePreviousMonth == null) totalExpensePreviousMonth = 0.0;
            Double totalBalancePreviousMonth = totalIncomePreviousMonth - totalExpensePreviousMonth;

            // Tính % thay đổi so với tháng trước đó
            Double incomeChange = calculatePercentageChange(totalIncomePreviousMonth, totalIncomeCurrentMonth);
            Double expenseChange = calculatePercentageChange(totalExpensePreviousMonth, totalExpenseCurrentMonth);
            Double balanceChange = calculatePercentageChange(totalBalancePreviousMonth, totalBalanceCurrentMonth);

            // Thêm vào kết quả
            totalIncome.add(totalIncomeCurrentMonth);
            totalIncome.add(incomeChange);

            totalExpense.add(totalExpenseCurrentMonth);
            totalExpense.add(expenseChange);

            totalBalance.add(totalBalanceCurrentMonth);
            totalBalance.add(balanceChange);
        } else {
            throw new IllegalArgumentException("Invalid type. Allowed values are 'month' or 'year'.");
        }

        List<String> revenuesConverted = revenues.stream()
            .map(value -> String.format("%.2f", value))
            .collect(Collectors.toList());
                    
        List<String> totalIncomeConverted = totalIncome.stream()
            .map(value -> String.format("%.2f", value))
            .collect(Collectors.toList());
                    
        List<String> totalExpenseConverted = totalExpense.stream()
            .map(value -> String.format("%.2f", value))
            .collect(Collectors.toList());
                    
        List<String> totalBalanceConverted = totalBalance.stream()
            .map(value -> String.format("%.2f", value))
            .collect(Collectors.toList());

        // Trả về kết quả
        return Map.of(
            "labels", labels,
            "revenues", revenuesConverted,
            "totalIncome", totalIncomeConverted,
            "totalExpense", totalExpenseConverted, 
            "totalBalance", totalBalanceConverted
        );
    }

    @Override
    public Map<String, Object> getBestSellers(String type, String date) {
        log.info("StatisticServiceImpl | getBestSellers | date: {}", date);
    
        List<Map<String, Object>> bestSellers = productRepository.findBestSellersByTypeAndDate(type, date);
    
        return Map.of("products", bestSellers);
    }

    @Override
    public Map<String, Object> getProductCategoryStatistics() {
        log.info("StatisticServiceImpl | getProductCategoryStatistics");

        List<Category> categories = categoryRepository.findAll();
    
        List<String> categoryNames = categories.stream()
                .map(Category::getName)
                .toList();
    
        List<Integer> categoryStocks = categories.stream()
                .map(Category::getStock)
                .toList();
    
        return Map.of(
                "categories", categoryNames,
                "data", categoryStocks
        );
    }

    @Override
    public Map<String, Object> getIncompleteOrders(String date) {
        log.info("StatisticServiceImpl | getIncompleteOrders | date: {}", date);

        List<Map<String, Object>> incompleteOrders = orderRepository.findIncompleteOrders(date);
    
        if (incompleteOrders.isEmpty()) {
            log.info("StatisticServiceImpl | getIncompleteOrders | No incomplete orders found for date: {}", date);
            return Map.of("orders", List.of());
        }
    
        log.info("StatisticServiceImpl | getIncompleteOrders | Found {} incomplete orders", incompleteOrders.size());
        return Map.of("orders", incompleteOrders);
    }

    private int getDaysInMonth(String date) {
        String[] parts = date.split("-");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);
        return YearMonth.of(year, month).lengthOfMonth();
    }

    private Double calculatePercentageChange(Double previous, Double current) {
        if (previous == 0) {
            return current == 0 ? 0.0 : 100.0; // Nếu previous = 0, trả về 100% nếu current > 0
        }
        return ((current - previous) / previous) * 100;
    }
}
