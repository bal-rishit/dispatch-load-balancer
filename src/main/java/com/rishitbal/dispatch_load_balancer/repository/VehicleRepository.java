package com.rishitbal.dispatch_load_balancer.repository;

import com.rishitbal.dispatch_load_balancer.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle,Long> {

    Optional<Vehicle> findByVehicleId(String vehicleId);
}
