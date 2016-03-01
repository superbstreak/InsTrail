package nwhack.instrail.com.instrail.Interface;

import java.util.ArrayList;

import nwhack.instrail.com.instrail.Model.InstData;
import nwhack.instrail.com.instrail.Model.Trail;

/**
 * Created by Rob on 2/27/2016.
 */
public interface DataListener {
    void onDataReceive(ArrayList<Trail> data,  String nextAction);
    void onDataLoading(String nextAction);
    void onDataError();
}
