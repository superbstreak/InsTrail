package nwhack.instrail.com.instrail;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import net.londatiga.android.instagram.Instagram;
import net.londatiga.android.instagram.InstagramRequest;
import net.londatiga.android.instagram.InstagramSession;
import net.londatiga.android.instagram.util.Cons;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import nwhack.instrail.com.instrail.Controller.BaseController;
import nwhack.instrail.com.instrail.Controller.InstagramController;
import nwhack.instrail.com.instrail.Controller.VolleyController;
import nwhack.instrail.com.instrail.Interface.DataListener;
import nwhack.instrail.com.instrail.Model.InstData;
import nwhack.instrail.com.instrail.Model.Trail;

public class MainActivity extends BaseActivity implements OnMapReadyCallback, View.OnClickListener, DataListener {

    private static Activity context;
    private boolean filterResume = false;

    private GoogleMap mMap;
    private Dialog filterPopup;
    private SupportMapFragment mapFragment;
    private LinearLayout accountButton;
    private LinearLayout photoButton;
    private LinearLayout cameraButton;
    private LinearLayout filterButton;
    private LinearLayout trailsButton;

    public static Activity getContext() {
        return context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        setCurrentDataListener(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        accountButton = (LinearLayout) this.findViewById(R.id.main_menu_account);
        photoButton = (LinearLayout) this.findViewById(R.id.main_menu_photo);
        cameraButton = (LinearLayout) this.findViewById(R.id.main_menu_camera);
        filterButton = (LinearLayout) this.findViewById(R.id.main_menu_filter);
        trailsButton = (LinearLayout) this.findViewById(R.id.main_menu_search);
        mapFragment.getMapAsync(this);
        accountButton.setOnClickListener(this);
        photoButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
        filterButton.setOnClickListener(this);
        trailsButton.setOnClickListener(this);
    }

    public void scrapeInstagram() {
        if (isFirstLoad) {
            String url = "https://api.instagram.com/v1/tags/" + Constant.TAG + "/media/recent?access_token=" + Constant.ZAMA_ZINGO_ACCESS_TOKEN;
            getScrapper().getTagRecentMedia(url, false);
            isFirstLoad = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        context = this;
        setCurrentDataListener(this);
        getScrapper();
        scrapeInstagram();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng vancouver = new LatLng(Constant.VANCOUVER_LAT, Constant.VANCOUVER_LON);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(vancouver));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(Constant.ZOOM_LEVEL));
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String name = marker.getTitle();
                int pos = 0;
                try {   // shortcut
                    if (trailMapper.containsKey(name)) {
                        pos = trailMapper.get(name);
                    } else {
                        searchPos(name);
                    }
                } catch (Exception e) {
                    searchPos(name);
                }
                Intent intent = new Intent(context, Photos.class);
                intent.putExtra(Constant.PHOTO_INTENT_TAG, Constant.PHOTO_TAG_TRAIL);
                intent.putExtra(Constant.TRAIL_POSITION_TAG, pos);
                startActivity(intent);
            }
        });
    }

    private int searchPos (String name) {
        int pos = 0;
        int size = trails.size();
        for (int i = 0; i < size; i++) {
            if(trails.get(i).getName().equals(name+"")) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    private class MapFilterLoader extends AsyncTask <Void,ArrayList<Trail>,ArrayList<Trail>> {

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            mMap.clear();
            ShowLoadingDialog();
        }

        @Override
        protected ArrayList<Trail> doInBackground(Void... params) {
            ArrayList<Trail> res = new ArrayList<Trail>();
            ArrayList<Trail> data = new ArrayList<Trail>();
            res.addAll(getTrails());
            int size = getTrails().size();
            if (currentFilter == 1 || currentFilter == 2) {
                Collections.sort(res, new Trail());
                for (int i = 0; i < 10; i++) {
                    if (currentFilter == 1) {
                        data.add(res.get(i));
                    } else if (currentFilter == 2){
                        data.add(res.get(size-1-i));
                    }
                }
            } else if (currentFilter == 0) {
                return getTrails();
            } else if (currentFilter == 3) {
                return getTrails();
            }

            return data;
        }

        @Override
        protected  void onPostExecute(ArrayList<Trail> result) {
            super.onPostExecute(result);
            if (result != null) {
                bulkAddMarkers(result);
            }
            if (LoadingDialog != null && LoadingDialog.isShowing()) {
                LoadingDialog.dismiss();
            }
        }
    }

    public void bulkAddMarkers(ArrayList<Trail> trailsx) {
        if (trailsx != null) {
            int size = trailsx.size();
            for (int i = 0; i < size; i++) {
                Trail trail = trailsx.get(i);
                String thumb = trail.getThumbnail();
                addMarkers(trail.getName(), trail.getLat(), trail.getLon(), thumb);
            }
        }
    }

    public void addMarkers(String name, double lat, double lon, String thumb) {
        if (mMap != null) {
            LatLng trail = new LatLng(lat, lon);
            MarkerOptions marker = new MarkerOptions().position(trail).title(name);
            BitmapDescriptor bit = BitmapDescriptorFactory.fromResource(R.drawable.hiking2);
            marker.icon(bit);
            mMap.addMarker(marker);
        }
    }

    @Override
    public void onClick(View view) {

        if (view.equals(accountButton)) {
            Intent intent = new Intent(MainActivity.this, Account.class);
            startActivity(intent);
        } else if (view.equals(photoButton)) {
            Intent intent = new Intent(MainActivity.this, Photos.class);
            intent.putExtra(Constant.PHOTO_INTENT_TAG, Constant.PHOTO_TAG_MAIN);
            startActivity(intent);
        } else if (view.equals(cameraButton)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, Constant.REQUEST_IMAGE_CAPTURE);
            }
        } else if (view.equals(filterButton)) {
            filterResume = true;
            showFilterPopUp();
        } else if (view.equals(trailsButton)) {
            Intent intent = new Intent(MainActivity.this, Trails.class);
            startActivity(intent);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
        }
    }

    @Override
    public void onDataReceive(ArrayList<Trail> data, String nextAction) {
        nextActionURL = nextAction;
        bulkAddMarkers(data);
        if (LoadingDialog != null && LoadingDialog.isShowing()) {
            LoadingDialog.dismiss();
        }
        scrapNextURL();
    }

    @Override
    public void onDataLoading(String nextAction) {
        nextActionURL = nextAction;
        ShowLoadingDialog();
        scrapNextURL();
    }

    @Override
    public void onDataError() {
        if (LoadingDialog != null && LoadingDialog.isShowing()) {
            LoadingDialog.dismiss();
        }
        scrapNextURL();
    }

    private void showFilterPopUp() {
        if (filterPopup != null && filterPopup.isShowing()) {
            filterPopup.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    MapFilterLoader a = new MapFilterLoader();
                    a.execute();
                }
            });
            filterPopup.dismiss();

        }
        if (!this.context.isFinishing()) {
            filterPopup = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            filterPopup.setContentView(R.layout.activity_filter);

            Window window = filterPopup.getWindow();

            Display display = context.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int w = size.x;
            int h = size.y;
            window.setLayout((int) w, (int) h);

            RadioGroup filterRadioButton = (RadioGroup) filterPopup.findViewById(R.id.radioFilter);
            final RadioButton filterNoFilter = (RadioButton) filterPopup.findViewById(R.id.radioNo);
            RadioButton filterTop = (RadioButton) filterPopup.findViewById(R.id.radioTop);
            RadioButton filterLow = (RadioButton) filterPopup.findViewById(R.id.radioLow);
            RadioButton filterMy = (RadioButton) filterPopup.findViewById(R.id.radioMyPic);
            RelativeLayout outside = (RelativeLayout) filterPopup.findViewById(R.id.filter_background);

            // no filter, top 10, low 10, my picture
            final int[] buttonID = new int[4];
            buttonID[0] = filterNoFilter.getId();
            buttonID[1] = filterTop.getId();
            buttonID[2] = filterLow.getId();
            buttonID[3] = filterMy.getId();
            filterNoFilter.setChecked(false);
            filterTop.setChecked(false);
            filterLow.setChecked(false);
            filterMy.setChecked(false);

            switch (currentFilter) {
                case 0:
                    filterNoFilter.setChecked(true);
                    break;
                case 1:
                    filterTop.setChecked(true);
                    break;
                case 2:
                    filterLow.setChecked(true);
                    break;
                case 3:
                    filterMy.setChecked(true);
                    break;

            }
            filterRadioButton.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == buttonID[0]) {
                        setCurrentFilter(0);
                    } else if (checkedId == buttonID[1]) {
                        setCurrentFilter(1);
                    } else if (checkedId == buttonID[2]) {
                        setCurrentFilter(2);
                    } else if (checkedId == buttonID[3]) {
                        setCurrentFilter(3);
                    }

                }
            });
            outside.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (filterPopup != null) {
                        filterPopup.dismiss();
                    }
                }
            });
            filterPopup.show();
            filterPopup.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    MapFilterLoader a = new MapFilterLoader();
                    a.execute();
                }
            });
            filterResume = true;
        }
    }


}