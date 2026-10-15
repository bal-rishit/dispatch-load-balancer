package com.rishitbal.dispatch_load_balancer.service;

import org.springframework.stereotype.Component;

@Component
public class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    public double calculateDistanceInKilometers(double lat1, double lon1, double lat2, double lon2){

        double latDiff = Math.toRadians(lat2-lat1); //difference b/w latitudes
        double lonDiff = Math.toRadians(lon2-lon1); //difference b/w longitudes

        // Haversine equations
        double a = Math.sin(latDiff/2)*Math.sin(latDiff/2) + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.sin(lonDiff/2)*Math.sin(lonDiff/2);

        double c = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));

        double r = EARTH_RADIUS_KM*c;

        return r;

    }

}
