package com.polylines.domain.robot;

public class GeoPointBuilder {

    private GeoPointBuilder() {
    }

    public static GeoPoint aGeoPoint(double lat, double lng){
        return new GeoPoint(lat, lng);
    }
}
