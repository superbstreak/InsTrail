package nwhack.instrail.com.instrail.Model;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import nwhack.instrail.com.instrail.Controller.VolleyController;
import nwhack.instrail.com.instrail.MainActivity;

/**
 * Created by Borislav on 2/28/2016.
 */
public class Instagram {

    private static final String CLIENT_ID = "d91dcfac9ed346478e76999806a15b59";
    private static final String CLIENT_SECRET = "cc8e2069c8c64e29900060d94475b71d";
    private static final String REDIRECT_URI = "com-instrail://instagramredirect";
    protected static final String ZAMA_ZINGO_ACCESS_TOKEN = "2257996576.cf0499d.08834443f30a4d278c28fcaf41af2f71";
    protected static final String ZAMA_ZINGO_USER_ID = "2257996576";
    protected static final String TAG = "vancouvertrails";

    protected static final int CALLS = 3;


    public VolleyController requestController;

    public Instagram() {
        requestController = MainActivity.getVolleyController();
    }

    public void getTagRecentMedia() {
        String url = "https://api.instagram.com/v1/tags/" + TAG + "/media/recent?access_token=" + ZAMA_ZINGO_ACCESS_TOKEN;
        final List<JSONObject> jsons = new ArrayList<>();
        for (int i = 0; i < CALLS; i++) {

            // Request a string response from the provided URL.
            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d("INSTAGRAM DATA", response.toString());
                            jsons.add(response);


                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("ERROR", error.toString());
                }
            });
            requestController.addToRequestQueue(stringRequest);
        }


    }

}
