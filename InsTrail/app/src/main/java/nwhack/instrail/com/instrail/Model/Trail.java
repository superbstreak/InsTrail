package nwhack.instrail.com.instrail.Model;

import java.util.ArrayList;

/**
 * Created by Rob on 2/28/2016.
 */
public class Trail {

    private String name;
    private ArrayList<InstData> data;
    private String thumbnail;

    public Trail(String name, ArrayList<InstData> d, String thumbnail) {
        this.name = name;
        data = new ArrayList<>();
        this.data = d;
        this.thumbnail = thumbnail;
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

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
