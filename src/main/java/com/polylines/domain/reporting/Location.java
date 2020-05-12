package com.polylines.domain.reporting;

public class Location {

    private double lat;
    private double lng;

    private Location() {
    }

    public Location(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

}
