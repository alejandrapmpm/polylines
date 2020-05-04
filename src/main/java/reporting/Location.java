package reporting;

public class Location {

    public double lat;
    public double lng;

    public Location() {
        //this is due to jackson error
    }

    public Location(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

}
