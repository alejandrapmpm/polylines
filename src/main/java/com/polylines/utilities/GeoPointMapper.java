package com.polylines.utilities;

import java.util.List;
import java.util.stream.Collectors;
import com.google.maps.model.LatLng;
import com.polylines.model.GeoPoint;
public class GeoPointMapper {

    public List<GeoPoint> map(List<LatLng> decodePath) {
        return decodePath.stream().map(this::newGeoPoint).collect(Collectors.toList());
    }

    private GeoPoint newGeoPoint(LatLng latLng) {
        return new GeoPoint(latLng.lat, latLng.lng);
    }
}
