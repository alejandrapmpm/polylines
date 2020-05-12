package com.polylines.infraestructure;

import com.polylines.domain.robot.GeoPoint;

public class DistanceCalculator {

    private static final int EARTH_RADIUS = 6371000;

    private DistanceCalculator() {
        throw new IllegalStateException("Utility class");
    }

    //result in meters
    public static double calculate(GeoPoint start, GeoPoint end) {

        double startLat = start.lat;
        double startLong = start.lng;
        double endLat = end.lat;
        double endLong = end.lng;

        double dLat = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversine(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversine(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    private static double haversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}
