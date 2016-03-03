package nwhack.instrail.com.instrail.Controller;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nwhack.instrail.com.instrail.BaseActivity;
import nwhack.instrail.com.instrail.Constant;
import nwhack.instrail.com.instrail.Model.InstData;
import nwhack.instrail.com.instrail.Model.Trail;

/**
 * Created by Borislav on 2/28/2016.
 */
public class InstagramController {

    private List<JSONObject> jsons = new ArrayList<>();

    public InstagramController() {
        BaseActivity.trailMapper = new HashMap<>();
        BaseActivity.mainData = new ArrayList<InstData>();
        BaseActivity.trails = new ArrayList<>();
    }

    public void getTagRecentMedia(String url, boolean isLast) {
        jsons = new ArrayList<>();
        final boolean isLastOne = isLast;
        // Request a string response from the provided URL.
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("INSTAGRAM DATA", response.toString());
                        jsons.add(response);
                        String nextAction = processNextUrl(response);
                        BaseActivity.currentCount += 1;
                        try {
                            if (nextAction != null) {
                                BaseActivity.nextActionURL = nextAction;
                                if (BaseActivity.getCurrentDataListener() != null) {
                                    if (isLastOne) {
                                        BaseActivity.getCurrentDataListener().onDataReceive(processAllJson(""), nextAction);
                                    } else {
                                        BaseActivity.getCurrentDataListener().onDataLoading(nextAction);
                                    }
                                }
                                BaseActivity.notifyObserver();
                            } else {
                                if (BaseActivity.getCurrentDataListener() != null) {
                                    BaseActivity.getCurrentDataListener().onDataError();
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ERROR", error.toString());

            }
        });
        BaseActivity.getVolleyController().addToRequestQueue(stringRequest);

    }

    public void getUserRecentMedia(String url) {
        jsons = new ArrayList<>();
        // Request a string response from the provided URL.
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("INSTAGRAM DATA", response.toString());
                        jsons.add(response);
                        String nextUrl = processNextUrl(response);
                        if (BaseActivity.getCurrentDataListener() != null) {
                            List<Trail> trails = processAllJson(Constant.USER_RECENT_MEDIA_ENDPOINT.toString());
//                            BaseActivity.user.setTrails(trails);
//                            BaseActivity.userTrails = trails;
                            BaseActivity.getCurrentDataListener().onDataReceive(trails, null);
                        }
                        BaseActivity.notifyObserver();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ERROR", error.toString());

            }
        });
        BaseActivity.getVolleyController().addToRequestQueue(stringRequest);


    }

    public String processNextUrl(JSONObject object) {
        String result = null;
        if (object != null) {
            try {
                JSONObject page = object.getJSONObject("pagination");
                result = page.getString("next_url");
            } catch (Exception e) {
                Log.e("TEST", "19" + e);
            }
        }
        return result;
    }

    private List<Trail> processAllJson(String endpoint) {
        List<Trail> tempData = new ArrayList<>();
        if (this.jsons != null) {
            int size = jsons.size();
            for (int i = 0; i < size; i++) {
                try {
                    JSONObject main = jsons.get(i);
                    JSONArray data = main.getJSONArray("data");
                    int jarrSize = data.length();
                    for (int j = 0; j < jarrSize; j++) {
                        JSONObject perUser = data.getJSONObject(j);

                        // Location
                        JSONObject location;
                        String name = "";
                        double latD = 0;
                        double lonD = 0;

                        // Image
                        JSONObject images = perUser.getJSONObject("images");
                        JSONObject low = images.getJSONObject("thumbnail");
                        JSONObject mid = images.getJSONObject("low_resolution");
                        JSONObject high = images.getJSONObject("standard_resolution");
                        InstData image = new InstData(low.getString("url"), mid.getString("url"), high.getString("url"));

                        try {
                            location = perUser.getJSONObject("location");
                            String lat = location.getString("latitude");
                            String lon = location.getString("longitude");
                            name = location.getString("name") + "";
                            latD = Double.parseDouble(lat);
                            lonD = Double.parseDouble(lon);
                            if (BaseActivity.trailMapper.containsKey(name)) {
                                Trail tr = BaseActivity.trails.get(BaseActivity.trailMapper.get(name));
                                if (!tr.getData().contains(image)) {
                                    tr.addData(image);
                                }
                            } else {
                                List<InstData> dataList = new ArrayList<InstData>();
                                dataList.add(image);
                                Trail newTrail = new Trail(name, dataList, mid.getString("url"), latD, lonD);
                                BaseActivity.trailMapper.put(name, BaseActivity.trails.size());
                                BaseActivity.trails.add(newTrail);
                                tempData.add(newTrail);
                            }
                            if (endpoint.equals(Constant.USER_RECENT_MEDIA_ENDPOINT.toString())) {

                                if (BaseActivity.user.getUserTrailMapper().containsKey(name)) {
                                    Trail tr = BaseActivity.user.getUserTrailMapper().get(BaseActivity.user.getUserTrailMapper().get(name));
                                    if (!tr.getData().contains(image)) {
                                        tr.addData(image);
                                    }
                                } else {
                                    List<InstData> dataList = new ArrayList<InstData>();
                                    dataList.add(image);
                                    Trail newTrail = new Trail(name, dataList, mid.getString("url"), latD, lonD);
                                    BaseActivity.user.getUserTrailMapper().put(name, newTrail);
                                    BaseActivity.user.getTrails().add(newTrail);
                                    tempData.add(newTrail);
                                }
                            }
                        } catch (Exception e) {
                        }

                        BaseActivity.mainData.add(image);
                    }
                } catch (Exception e) {
                    Log.e("EXCE", e + "");
                }
            }
        }
        return tempData;
    }

    public List<JSONObject> getJson() {
        return this.jsons;
    }
}
