package com.polylines.infraestructure;

import java.util.List;
import java.util.stream.Collectors;
import com.google.maps.model.LatLng;
import com.polylines.domain.robot.GeoPoint;

public class GeoPointMapper {

    private GeoPointMapper() {
    }

    public static List<GeoPoint> map(List<LatLng> decodePath) {
        return decodePath.stream().map(GeoPointMapper::toGeoPoint).collect(Collectors.toList());
    }

    private static GeoPoint toGeoPoint(LatLng latLng) {
        return new GeoPoint(latLng.lat, latLng.lng);
    }
}
