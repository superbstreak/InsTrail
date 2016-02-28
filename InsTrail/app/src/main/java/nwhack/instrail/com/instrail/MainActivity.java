package nwhack.instrail.com.instrail;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import net.londatiga.android.instagram.Instagram;
import net.londatiga.android.instagram.InstagramRequest;
import net.londatiga.android.instagram.InstagramSession;

import java.util.ArrayList;

import nwhack.instrail.com.instrail.Adapter.FilterAdapter;
import nwhack.instrail.com.instrail.Controller.BaseController;
import nwhack.instrail.com.instrail.Controller.VolleyController;
import nwhack.instrail.com.instrail.Interface.DataListener;
import nwhack.instrail.com.instrail.Model.InstData;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, DataListener {

    private Activity context;
    private Context appContext;
    private Dialog filterPopup;
    private static int currentFilter = 0;

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private LinearLayout accountButton;
    private LinearLayout photoButton;
    private LinearLayout cameraButton;
    private LinearLayout filterButton;
    private LinearLayout trailsButton;

    private static final String CLIENT_ID = "d91dcfac9ed346478e76999806a15b59";
    private static final String CLIENT_SECRET = "cc8e2069c8c64e29900060d94475b71d";
    private static final String REDIRECT_URI = "com-instrail://instagramredirect";
    protected static final String ZAMA_ZINGO_ACCESS_TOKEN = "2257996576.cf0499d.08834443f30a4d278c28fcaf41af2f71";

    protected Instagram mInstagram;
    protected InstagramSession mInstagramSession;
    protected InstagramRequest instagramRequest;

    private static VolleyController requestController;
    private ArrayList<InstData> mainData = new ArrayList<>();

    // Singleton getters
    public Context getAppContext() {
        return this.appContext;
    }

    public Activity getContext() {
        return this.context;
    }

    public ArrayList<InstData> getMainData() {
        return this.mainData;
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

    // ========================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInstagram = new Instagram(this, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI);

        context = this;
        appContext = this.getApplicationContext();
        BaseController.appContext = getApplicationContext();

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

        String url = "http://icons.iconarchive.com/icons/iconka/meow/256/cat-grumpy-icon.png";
        for (int i = 0; i < 999; i++) {
            mainData.add(new InstData(url, url, url));
        }

        scrapeInstagram();
    }

    public void scrapeInstagram() {
        //TODO
        instagramRequest = new InstagramRequest();
        //createRequest(String method, String endpoint, List < NameValuePair > params)
    }

    @Override
    public void onResume() {
        super.onResume();
        context = this;
        appContext = this.getApplicationContext();
        BaseController.appContext = getApplicationContext();
        getVolleyController(); // place here to make sure it never dies
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
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onClick(View view) {

        if (view.equals(accountButton)) {
            Toast.makeText(context, "account clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, Account.class);
            startActivity(intent);
        } else if (view.equals(photoButton)) {
            Toast.makeText(context, "photo clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, Photos.class);
            intent.putExtra(Constant.PHOTO_INTENT_TAG, Constant.PHOTO_TAG_MAIN);
            startActivity(intent);
        } else if (view.equals(cameraButton)) {
            // TODO: forbid user to take photos unless they are logged in
            Toast.makeText(context, "camera clicked", Toast.LENGTH_SHORT).show();
            takePicture();
        } else if (view.equals(filterButton)) {
            showFilterPopUp();
        } else if (view.equals(trailsButton)) {
            Toast.makeText(context, "trail clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, Trails.class);
            startActivity(intent);
        }
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
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
    public void onDataReceive() {

    }

    @Override
    public void onDataLoading() {

    }

    @Override
    public void onDataError() {

    }


    private void showFilterPopUp() {
        if (filterPopup != null && filterPopup.isShowing()) {
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

            final ListView listview = (ListView) filterPopup.findViewById(R.id.filter_listview);
            final ImageView close_filter = (ImageView) filterPopup.findViewById(R.id.filter_close);

            // no filter, top 10, low 10, my picture
            final String[] filterStr = {"No Filter", "Top 10", "Low 10", "My Picture"};
            final FilterAdapter adapter = new FilterAdapter(this, filterStr, currentFilter);
            listview.setAdapter(adapter);

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    currentFilter = position;
                    adapter.notifyDataSetChanged();
                }
            });

            close_filter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (filterPopup != null) {
                        filterPopup.dismiss();
                    }
                }
            });

            filterPopup.show();
        }
    }
}
