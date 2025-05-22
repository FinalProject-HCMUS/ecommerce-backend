package com.hcmus.ecommerce_backend.order.repository;

import com.hcmus.ecommerce_backend.order.model.entity.Order;
import com.hcmus.ecommerce_backend.order.model.enums.Status;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    List<Order> findByCustomerId(String customerId);
    
    boolean existsByCustomerIdAndId(String customerId, String id);

    @Query("SELECT o FROM Order o WHERE " +
           "(:keyword IS NULL OR " +
           "LOWER(o.firstName) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR " +
           "LOWER(o.lastName) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR " +
           "LOWER(o.phoneNumber) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) " +
           "AND (:status IS NULL OR o.status = :status)")
    Page<Order> searchOrders(@Param("keyword") String keyword, 
                             @Param("status") Status status,
                             Pageable pageable);

    @Query(value = """
        SELECT o.id AS orderId, 
            CONCAT(o.first_name, ' ', o.last_name) AS buyerName, 
            TO_CHAR(o.created_at, 'YYYY-MM-DD') AS purchaseDate, 
            o.payment_method AS paymentMethod, 
            SUM(od.quantity * p.price) AS revenue
        FROM orders o
        JOIN order_detail od ON o.id = od.order_id
        JOIN products p ON od.product_id = p.id
        WHERE o.status NOT IN ('DELIVERED', 'REFUND') 
        AND (:date IS NULL OR TO_CHAR(o.created_at, 'MM-YYYY') = :date)
        GROUP BY o.id, o.first_name, o.last_name, o.created_at, o.payment_method
        ORDER BY o.created_at DESC
    """, nativeQuery = true)
    List<Map<String, Object>> findIncompleteOrders(@Param("date") String date);

    @Query(value = """
        SELECT SUM(o.sub_total)
        FROM orders o
        WHERE EXTRACT(YEAR FROM o.delivery_date) = :year
    """, nativeQuery = true)
    Double sumSubTotalByYear(@Param("year") int year);

    @Query(value = """
        SELECT SUM(o.sub_total)
        FROM orders o
        WHERE EXTRACT(DAY FROM o.delivery_date) = :day
        AND TO_CHAR(o.delivery_date, 'MM-YYYY') = :monthYear
    """, nativeQuery = true)
    Double sumSubTotalByDayAndMonth(@Param("day") int day, @Param("monthYear") String monthYear);
    
    @Query(value = """
        SELECT SUM(o.sub_total)
        FROM orders o
        WHERE EXTRACT(MONTH FROM o.delivery_date) = :month
        AND EXTRACT(YEAR FROM o.delivery_date) = :year
    """, nativeQuery = true)
    Double sumSubTotalByMonthAndYear(@Param("month") int month, @Param("year") int year);
}