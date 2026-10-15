package com.rishitbal.dispatch_load_balancer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DistanceCalculatorTest {

    private final DistanceCalculator distanceCalculator = new DistanceCalculator();

    @Test
    void returnsZeroForTheSameLocation() {
        double distance = distanceCalculator.calculateDistanceInKilometers(
                12.9716,
                77.5946,
                12.9716,
                77.5946
        );

        assertEquals(0.0, distance, 0.000001);
    }

    @Test //The second test checks that one degree of longitude at the equator is approximately 111.2 km
    void calculatesKnownDistanceBetweenCoordinates() {
        double distance = distanceCalculator.calculateDistanceInKilometers(
                0.0,
                0.0,
                0.0,
                1.0
        );

        assertEquals(111.2, distance, 0.1);
    }
}