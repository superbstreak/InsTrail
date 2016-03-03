package nwhack.instrail.com.instrail.Model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Rob on 2/28/2016.
 */
public class Trail implements Comparator<Trail> {

    private String name;
    private ArrayList<InstData> data;
    private String thumbnail;
    private double lat;
    private double lon;

    public Trail () {

    }

    public Trail(String name, ArrayList<InstData> d, String thumbnail, double lat, double lon) {
        this.name = name;
        data = new ArrayList<>();
        this.data = d;
        this.thumbnail = thumbnail;
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<InstData> getData() {
        return data;
    }

    public void setData(ArrayList<InstData> data) {
        this.data = data;
    }

    public void addData(InstData img) {
        if (this.data == null) {
            this.data = new ArrayList<>();
        }
        this.data.add(img);
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public int compare(Trail lhs, Trail rhs) {
        if (lhs.getData().size() > rhs.getData().size()) {
            return -1;
        } else if (lhs.getData().size() < rhs.getData().size()) {
            return 1;
        }

        return 0;
    }

    @Override
    public boolean equals(Object object) {
        return false;
    }

}
