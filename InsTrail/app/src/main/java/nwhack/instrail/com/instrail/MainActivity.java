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

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, DataListener {

    int ZOOM_LEVEL = 9;

    private static Activity context;
    private static DataListener currentDataListener;
    private Context appContext;
    private Dialog filterPopup;
    private static int currentFilter = 0;

    private static GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private LinearLayout accountButton;
    private LinearLayout photoButton;
    private LinearLayout cameraButton;
    private LinearLayout filterButton;
    private LinearLayout trailsButton;
    private Dialog LoadingDialog;

    private static final String CLIENT_ID = "d91dcfac9ed346478e76999806a15b59";
    private static final String CLIENT_SECRET = "cc8e2069c8c64e29900060d94475b71d";
    private static final String REDIRECT_URI = "com-instrail://instagramredirect";
    protected static final String ZAMA_ZINGO_ACCESS_TOKEN = "2257996576.cf0499d.08834443f30a4d278c28fcaf41af2f71";
    protected static final String ZAMA_ZINGO_USER_ID = "2257996576";
    protected static final String TAG = "vancouvertrails";

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    protected Instagram mInstagram;
    protected InstagramSession mInstagramSession;
    protected InstagramRequest instagramRequest;

    public static ImageLoader il;
    private static VolleyController requestController;
    public static ArrayList<InstData> mainData = new ArrayList<>();
    private ArrayList<InstData> localData = new ArrayList<>();
    public static ArrayList<Trail> trails = new ArrayList<>();

    public static HashMap<String, Integer> trailMapper = new HashMap<>();
    public static InstagramController scrapper;
    private int currentCount = 1;
    private final int MAX_CALL = 20;
    public static boolean isFirstLoad = true;
    private boolean filterResume = false;
    public static String nextActionURL;

    // Singleton getters
    public Context getAppContext() {
        return this.appContext;
    }

    public static Activity getContext() {
        return context;
    }

    public static DataListener getCurrentDataListener() {
        return currentDataListener;
    }

    public static void setCurrentDataListener(DataListener datalist) {
        currentDataListener = datalist;
    }


    public static VolleyController getVolleyController() {
        if (requestController == null) {
            requestController = new VolleyController();
        }
        return requestController;
    }

    public static int getCurrentFilter() {
        return currentFilter;
    }

    public static void setCurrentFilter(int select) {
        currentFilter = select;
    }

    public ArrayList<Trail> getTrails() {
        return this.trails;
    }

    public void setLocalData(ArrayList<InstData> data) {
        this.localData = data;
    }

    public ArrayList<InstData> getLocalData() {
        return this.localData;
    }

    // ========================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInstagram = new Instagram(this, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI);

        context = this;
        appContext = this.getApplicationContext();
        BaseController.appContext = getApplicationContext();
        setCurrentDataListener(this);
        scrapper = new InstagramController();

        il = ImageLoader.getInstance();
        il.init(ImageLoaderConfiguration.createDefault(context));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
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
            String url = "https://api.instagram.com/v1/tags/" + TAG + "/media/recent?access_token=" + ZAMA_ZINGO_ACCESS_TOKEN;
            scrapper.getTagRecentMedia(url, false);
            isFirstLoad = false;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        context = this;
        appContext = this.getApplicationContext();
        BaseController.appContext = getApplicationContext();
        setCurrentDataListener(this);
        getVolleyController(); // place here to make sure it never dies
        if (il == null) {
            il = ImageLoader.getInstance();
            il.init(ImageLoaderConfiguration.createDefault(context));
        }
        scrapeInstagram();

        if (filterPopup != null) {
            filterPopup.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    MapFilterLoader a = new MapFilterLoader();
                    a.execute();
                }
            });
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng vancouver = new LatLng(49.485079, -122.985231);
//        mMap.addMarker(new MarkerOptions().position(vancouver).title("Marker in Vancouver"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(vancouver));

        mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));
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
            res.addAll(trails);
            int size = trails.size();
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
                return trails;
            } else if (currentFilter == 3) {
                return trails;
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

    public static void bulkAddMarkers(ArrayList<Trail> trailsx) {
        if (trailsx != null) {
            int size = trailsx.size();
            for (int i = 0; i < size; i++) {
                Trail trail = trailsx.get(i);
                String thumb = trail.getThumbnail();
                addMarkers(trail.getName(), trail.getLat(), trail.getLon(), thumb);
            }
        }
    }

    public static void addMarkers(String name, double lat, double lon, String thumb) {
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
//            Toast.makeText(context, "account clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, Account.class);
            startActivity(intent);
//            isFirstLoad = true;
        } else if (view.equals(photoButton)) {
//            Toast.makeText(context, "photo clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, Photos.class);
            intent.putExtra(Constant.PHOTO_INTENT_TAG, Constant.PHOTO_TAG_MAIN);
            startActivity(intent);
        } else if (view.equals(cameraButton)) {
//            Toast.makeText(context, "camera clicked", Toast.LENGTH_SHORT).show();
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else if (view.equals(filterButton)) {
            filterResume = true;
            showFilterPopUp();
        } else if (view.equals(trailsButton)) {
//            Toast.makeText(context, "trail clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, Trails.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            mImageView.setImageBitmap(imageBitmap);
        }
    }

    @Override
    public void onDataReceive(ArrayList<InstData> data, String nextAction) {
//        Log.e("asdfasdfas", scrapper.getJson().size() + "");
        nextActionURL = nextAction;
        bulkAddMarkers(trails);
//        Toast.makeText(getAppContext(), data.size()+" SAFE "+this.mainData.size(), Toast.LENGTH_LONG).show();
        if (LoadingDialog != null && LoadingDialog.isShowing()) {
            LoadingDialog.dismiss();
        }
    }

    @Override
    public void onDataLoading(String nextAction) {
//        Log.e("REACHED ", currentCount+"   "+nextAction+"");
        nextActionURL = nextAction;
        ShowLoadingDialog();
        if (nextAction != null && this.currentCount < MAX_CALL) {
            if (scrapper == null) {
                scrapper = new InstagramController();
            }
            boolean isLast = (this.currentCount == MAX_CALL - 1);
            scrapper.getTagRecentMedia(nextAction, isLast);
            currentCount+=1;
        }
    }

    @Override
    public void onDataError() {
        currentCount+=1;
    }


    private void showFilterPopUp() {
        if (filterPopup != null && filterPopup.isShowing()) {
            filterPopup.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
//                    Log.e("ASDF", "ADFGADS");
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
//                    Log.e("ASDF", "ADFGADS");
                    MapFilterLoader a = new MapFilterLoader();
                    a.execute();
                }
            });

            filterResume = true;
        }
    }


    public void ShowLoadingDialog() {
        if (LoadingDialog != null && LoadingDialog.isShowing()) {
        } else {
            LoadingDialog = new Dialog(this, android.R.style.Theme_Translucent);
            LoadingDialog.setCancelable(false);
            LoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            LoadingDialog.setContentView(R.layout.load_hub);
            Window window = LoadingDialog.getWindow();
            Display display = this.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int w = size.x;
            window.setLayout((int) (w * 0.30), (int) (w * 0.30));
            LoadingDialog.show();
        }
    }
}