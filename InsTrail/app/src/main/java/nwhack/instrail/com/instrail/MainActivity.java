package nwhack.instrail.com.instrail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import nwhack.instrail.com.instrail.Controller.BaseController;
import nwhack.instrail.com.instrail.Controller.VolleyController;
import nwhack.instrail.com.instrail.Interface.DataListener;
import nwhack.instrail.com.instrail.Model.InstData;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, DataListener {

    private Activity context;
    private Context appContext;

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private LinearLayout accountButton;
    private LinearLayout photoButton;
    private LinearLayout cameraButton;
    private LinearLayout filterButton;
    private LinearLayout trailsButton;

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

    // ========================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            Toast.makeText(context, "filter clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, Filter.class);
            startActivity(intent);
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
}
