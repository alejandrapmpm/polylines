package service;

import java.util.List;
import java.util.stream.Collectors;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import model.GeoPoint;

public class RobotMovementService {

    EncodedPolyline encoder;

    public RobotMovementService(EncodedPolyline encodedPolyline) {
        encoder = encodedPolyline;
    }


    public List<GeoPoint> decode() {
        List<GeoPoint> points = map(encoder.decodePath());
        System.out.println("Decoded: " + points);
        return points;
    }

    private List<GeoPoint> map(List<LatLng> decodePath) {
        return decodePath.stream().map(this::newGeoPoint).collect(Collectors.toList());
    }

    private <R> GeoPoint newGeoPoint(LatLng latLng) {
        return new GeoPoint(latLng.lat, latLng.lng);
    }
}
