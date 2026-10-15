package com.rishitbal.dispatch_load_balancer.service;

import com.rishitbal.dispatch_load_balancer.domain.DeliveryOrder;
import com.rishitbal.dispatch_load_balancer.domain.Vehicle;
import com.rishitbal.dispatch_load_balancer.dto.DeliveryOrderRequest;
import com.rishitbal.dispatch_load_balancer.dto.VehicleRequest;
import com.rishitbal.dispatch_load_balancer.repository.DeliveryOrderRepository;
import com.rishitbal.dispatch_load_balancer.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DispatchDataService {
    private final DeliveryOrderRepository deliveryOrderRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public void upsertOrders(List<DeliveryOrderRequest> requests){
        for (DeliveryOrderRequest request : requests) {
            DeliveryOrder order = deliveryOrderRepository.findByOrderId(request.orderId()).orElseGet(()-> new DeliveryOrder(
                   request.orderId(),
                   request.latitude(),
                   request.longitude(),
                   request.address(),
                   request.packageWeight(),
                   request.priority()
            ));

            order.setLongitude(request.longitude());
            order.setLatitude(request.latitude());
            order.setAddress(request.address());
            order.setPackageWeight(request.packageWeight());
            order.setPriority(request.priority());

            deliveryOrderRepository.save(order);
        }
    }

    @Transactional
    public void upsertVehicles(List<VehicleRequest> requests){
        for (VehicleRequest request: requests) {
            Vehicle vehicle = vehicleRepository.findByVehicleId(request.vehicleId()).orElseGet(()-> new Vehicle(
                    request.vehicleId(),
                    request.capacity(),
                    request.currentLatitude(),
                    request.currentLongitude(),
                    request.currentAddress()
            ));

            vehicle.setCapacity(request.capacity());
            vehicle.setCurrentLatitude(request.currentLatitude());
            vehicle.setCurrentLongitude(request.currentLongitude());
            vehicle.setCurrentAddress(request.currentAddress());

            vehicleRepository.save(vehicle);
        }
    }


}
