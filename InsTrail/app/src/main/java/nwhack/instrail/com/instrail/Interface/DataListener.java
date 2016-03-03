package nwhack.instrail.com.instrail.Interface;

import java.util.List;

import nwhack.instrail.com.instrail.Model.Trail;

/**
 * Created by Rob on 2/27/2016.
 */
public interface DataListener {
    void onDataReceive(List<Trail> data,  String nextAction);
    void onDataLoading(String nextAction);
    void onDataError();
}
