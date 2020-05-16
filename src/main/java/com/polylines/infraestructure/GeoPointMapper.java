package com.polylines.infraestructure;

import static com.polylines.domain.robot.GeoPointBuilder.aGeoPoint;

import java.util.List;
import java.util.stream.Collectors;
import com.google.maps.model.LatLng;
import com.polylines.domain.robot.GeoPoint;

public class GeoPointMapper {

    private GeoPointMapper() {
    }

    public static List<GeoPoint> map(List<LatLng> decodedPath) {
        return decodedPath.stream().map(GeoPointMapper::toGeoPoint).collect(Collectors.toList());
    }

    private static GeoPoint toGeoPoint(LatLng latLng) {
        return aGeoPoint(latLng.lat, latLng.lng);
    }
}
