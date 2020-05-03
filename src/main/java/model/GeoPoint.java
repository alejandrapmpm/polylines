package model;

import java.util.Objects;

public class GeoPoint {
    private final double lat;
    private final double lng;

    public GeoPoint(double lat, double lng) {

        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "GeoPoint{" +
                "lat=" + lat +
                ", lng=" + lng +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeoPoint geoPoint = (GeoPoint) o;
        return Double.compare(geoPoint.lat, lat) == 0 &&
                Double.compare(geoPoint.lng, lng) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lng);
    }
}
