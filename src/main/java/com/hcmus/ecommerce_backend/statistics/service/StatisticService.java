package com.hcmus.ecommerce_backend.statistics.service;

import java.util.Map;

public interface StatisticService {
    Map<String, Object> getSalesAnalysis(String type, String date);

    Map<String, Object> getBestSellers(String type, String date);

    Map<String, Object> getProductCategoryStatistics();
    
    Map<String, Object> getIncompleteOrders(String date);
}
