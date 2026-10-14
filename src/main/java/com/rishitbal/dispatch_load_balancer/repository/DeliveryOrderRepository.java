package com.rishitbal.dispatch_load_balancer.repository;

import com.rishitbal.dispatch_load_balancer.domain.DeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder,Long> {

    Optional<DeliveryOrder> findByOrderId(String orderId);
}
