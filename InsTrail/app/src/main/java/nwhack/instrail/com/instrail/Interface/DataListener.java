package nwhack.instrail.com.instrail.Interface;

import java.util.ArrayList;

import nwhack.instrail.com.instrail.Model.InstData;

/**
 * Created by Rob on 2/27/2016.
 */
public interface DataListener {
    void onDataReceive(ArrayList<InstData> data);
    void onDataLoading();
    void onDataError();
}
