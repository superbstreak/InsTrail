package nwhack.instrail.com.instrail;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nwhack.instrail.com.instrail.Model.Trail;

@SuppressWarnings("deprecation")
public class VisualizeActivity extends BaseActivity implements SensorEventListener, SurfaceHolder.Callback, LocationListener {

    private Activity mContext;
    private RelativeLayout freeDraw;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private ImageView mCompassIndicator;
    private Camera acam = null;

    private SensorManager mSensorManager;
    private Sensor mGyroSensor;
    private Sensor mAcc;
    private Sensor mMag;
    private LocationManager lm;
    private Location location;

    //time smoothing constant for low-pass filter 0 ≤ alpha ≤ 1 ; a smaller value basically means more smoothing
    // http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
    private final float LOW_PASS_ALPHA = 0.25f;
    private final float HIGH_PASS_ALPHA = 0.8f;
    private final int MAX_SHOW = 15;
    private final int MAX_DIST = 250; //km
    private final float PI = 3.1415926f;
    private final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 800; // The minimum distance to change updates in meters
    private final long MIN_TIME_BW_UPDATES = 1000 * 60 * 5; // The minimum time between updates in milliseconds

    private boolean inPreview = false;
    private double currentLAT;
    private double currentLON;
    private boolean hasGyroscope = false;
    private boolean hasAcc = false;
    private boolean hasMag = false;
    private boolean isTracking;
    private int[] device = {0, 0};
    private float[] mGravity;
    private float[] mGeomagnetic;
    private float screenVisCen = 0;
    private Float azimut;
    private Float previousAzu = null;
    private List<aDataPoint> data = new ArrayList<>();
    private List<View> texts = new ArrayList<>();
    private double maxDist = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualize);
        setUpLocationManager();
        freeDraw = (RelativeLayout) this.findViewById(R.id.visualize_free_draw);
        mSurfaceView = (SurfaceView) findViewById(R.id.visualize_surface_camera);
        mCompassIndicator = (ImageView) findViewById(R.id.compass_image);
        mContext = this;
        isTracking = false;
        updateCompassIndicator(0);
        updatePosition();
        setUpSensor();
        registerSensor();
        device[0] = BaseActivity.getDeviceMetric()[0];
        device[1] = BaseActivity.getDeviceMetric()[1];
        new MapFilterLoader().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
    }

    // ============================================================================================

    private void updateCompassIndicator(final float azu) {
        if (mCompassIndicator != null) {
            if (azu > 0) {
                final float deg = (int) (azu * 360 / PI);
                mCompassIndicator.animate().rotation(deg).setInterpolator(new LinearInterpolator()).setDuration(0);
            } else {
                final float deg = 360 - (azu * -360 / PI);
                mCompassIndicator.animate().rotation(deg).setInterpolator(new LinearInterpolator()).setDuration(0);
            }
        }
    }

    private void setUpLocationManager() {
        try {
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (isGPSEnabled) {
                Log.d("VIS", "Application use GPS Service");
                lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        this);

                if (lm != null) {
                    location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updatePosition();
                }
            } else if (isNetworkEnabled) { // Try to get location if you Network Service is enabled
                Log.d("VIS", "Application use Network State to get GPS coordinates");
                lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        this);
                if (lm != null) {
                    location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updatePosition();
                }
            }

        } catch (Exception e) {
            Log.d("VIS", "LOCATION ISSUE " + e);
        }
    }

    private List<aDataPoint> processTrailsData(List<Trail> trr) {
        data = new ArrayList<>();
        if (trr != null) {
            int size = trr.size();
            for (int i = 0; i < size; i++) {
                Trail tr = trr.get(i);
                final String name = tr.getName();
                final String thumb = tr.getThumbnail();
                final double lat = tr.getLat();
                final double lon = tr.getLon();
                final aDataPoint pt = new aDataPoint(calculateDefaultOffset(
                        currentLAT, currentLON, lat, lon),
                        calculateDistance(currentLAT, currentLON, lat, lon),
                        i, name, thumb);
                data.add(pt);
            }
        }
        return data;
    }

    private void updatePosition() {
        if (location == null) {
            setUpLocationManager();
        }
        try {
            // need to be able to update user location whne changed
            currentLON = location.getLongitude();
            currentLAT = location.getLatitude();
            Log.d("VIS", "LAT: " + currentLAT + " LONG: " + currentLON);
        } catch (Exception e) {
        }
    }

    private void drawInitialData() {
        if (data != null && freeDraw != null) {
            texts = new ArrayList<>();
            int size = data.size();
            int maxShow = Math.min(size, MAX_SHOW);
            int deviceHeightHalf = device[1] / 2;
            for (int i = 0; i < size; i++) {
                aDataPoint point = data.get(i);
                if (point.dist > MAX_DIST) {
                    //ignore
                } else if (maxShow == 0) {
                    break;
                } else {
                    maxShow -= 1;
                    @SuppressLint("InflateParams")
                    View view = getLayoutInflater().inflate(R.layout.visualize_location, null);
                    final ImageView valueTV = (ImageView) view.findViewById(R.id.vis_loc_img);
                    final TextView title = (TextView) view.findViewById(R.id.vis_loc_txtTitle);
                    final TextView dists = (TextView) view.findViewById(R.id.vis_loc_txtDistance);
                    title.setText(point.name + "");
                    dists.setText((int) point.dist + " km");
                    view.setX(point.x);
                    view.setY((int) (deviceHeightHalf * (1.05 - (point.dist / maxDist))));
                    view.setTag(i);
                    ImageRequest request = new ImageRequest(point.thumbnail,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {
                                    valueTV.setImageBitmap(bitmap);
                                }
                            }, 0, 0, null,
                            new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {
                                    valueTV.setImageResource(R.drawable.ic_photo);
                                }
                            });
                    BaseActivity.getVolleyController().addToRequestQueue(request);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Intent intent = new Intent(mContext, Photos.class);
                                intent.putExtra(Constant.PHOTO_INTENT_TAG, Constant.PHOTO_TAG_TRAIL);
                                intent.putExtra(Constant.TRAIL_POSITION_TAG, (int) v.getTag());
                                startActivity(intent);
                            } catch (Exception e) {
                                Log.d("VIS", "FAILED TO TRANSIT " + e);
                            }
                        }
                    });
                    texts.add(view);
                    freeDraw.addView(view);
                }
            }
        }
    }

    private double calculateDistance(final double lat1, final double lon1, final double lat2, final double lon2) {
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * (Math.sin(dlon / 2)), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = 6371 * c;
        if (d < MAX_DIST && maxDist < d) {
            maxDist = d;
        }
        return d;
    }

    private void updateDataPos(final float azu) {
        if (data != null && texts != null) {
            final float movement = azu * device[0];
            int size = texts.size();
            for (int i = 0; i < size; i++) {
                final View currentView = texts.get(i);
                final float currentTVX = currentView.getX();
                final int tag = (int) currentView.getTag();
                if (hasGyroscope) {
                    updateCompassIndicator(azu);
                    final float newTVX = data.get(tag).x - 2 * movement;
                    currentView.setX(newTVX);
                } else if (hasAcc && hasMag) {
                    updateCompassIndicator(azu);
                    final float newTVX = data.get(tag).x - movement;
                    final float diff = Math.abs(currentTVX - newTVX);
                    if (diff > device[0] / 20) {
                        currentView.setX(newTVX);
                    }
                }
            }
        }
    }

    private int calculateDefaultOffset(double currentlat, double currentlon, double lat, double lon) {
        // θ = atan2(sin(Δlong)*cos(lat2), cos(lat1)*sin(lat2) − sin(lat1)*cos(lat2)*cos(Δlong))
        final double deltaLon = lon - currentlon;
        final double sinDeltaLon = Math.sin(deltaLon);
        final double cosLat = Math.cos(lat);
        final double cosLat1sinLat2 = Math.cos(currentlat) * Math.sin(lat);
        final double sinLat1cosLat2cosDeltaLon = Math.sin(currentlat) * Math.cos(lat) * Math.cos(deltaLon);
        final double theta = Math.atan2(sinDeltaLon * cosLat, cosLat1sinLat2 - sinLat1cosLat2cosDeltaLon);
        return (int) (screenVisCen + theta * device[0]);
    }

    private void unregisterSensor() {
        if (isTracking) {
            if (hasGyroscope) {
                mSensorManager.unregisterListener(this, mGyroSensor);
            } else if (hasAcc && hasMag) {
                mSensorManager.unregisterListener(this, mAcc);
                mSensorManager.unregisterListener(this, mMag);
            }
            isTracking = false;
        }
    }

    private void registerSensor() {
        if (!isTracking) {
            if (hasGyroscope) {
                mSensorManager.registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_GAME);
            } else if (hasMag && hasAcc) {
                mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_GAME);
                mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_GAME);
            }
            isTracking = true;
        }
    }

    private void setUpSensor() {
        getAvailableSensors();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (hasGyroscope) {
            mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        } else if (hasAcc && hasMag) {
            mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
    }

    private void getAvailableSensors() {
        PackageManager packageManager = getPackageManager();
        hasGyroscope = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        hasAcc = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        hasMag = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
    }

    /**
     *  http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
     *  http://developer.android.com/reference/android/hardware/SensorEvent.html#values
     */
    @SuppressWarnings("unused")
    protected float[] lowPass(float[] input, float[] output) {
        if (output == null) {
            return input;
        }

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + LOW_PASS_ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    protected float[] highPass(float[] gravity, final float[] values) {
        if (gravity == null) return values;
        gravity[0] = HIGH_PASS_ALPHA * gravity[0] + (1 - HIGH_PASS_ALPHA) * values[0];
        gravity[1] = HIGH_PASS_ALPHA * gravity[1] + (1 - HIGH_PASS_ALPHA) * values[1];
        gravity[2] = HIGH_PASS_ALPHA * gravity[2] + (1 - HIGH_PASS_ALPHA) * values[2];
        return gravity;
    }

    private void unregisterLocationUpdate() {
        if (lm != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            lm.removeUpdates(this);
        }
    }

    // =============================================================================================

    @Override
    public void onResume() {
        super.onResume();
        mContext = this;
        device[0] = BaseActivity.getDeviceMetric()[0];
        device[1] = BaseActivity.getDeviceMetric()[1];
        freeDraw.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        registerSensor();
        setUpLocationManager();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterSensor();
        unregisterLocationUpdate();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        acam = Camera.open();
        acam.setDisplayOrientation(90);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(inPreview){
            acam.stopPreview();
            inPreview = false;
        }

        if (acam != null){
            try {
                Camera.Size preSize = getOptimalPreviewSize(acam.getParameters().getSupportedPreviewSizes(),device[0],device[1]);
                Camera.Parameters parameters = acam.getParameters();
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFlashMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
                parameters.setPreviewSize(preSize.width,preSize.height);
                acam.setParameters(parameters);
                acam.setPreviewDisplay(mSurfaceHolder);
                acam.startPreview();
                inPreview = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        acam.stopPreview();
        acam.release();
        acam = null;
        inPreview = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (hasGyroscope && event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            updateDataPos((float)Math.asin(-event.values[1])*2);
        } else if (hasAcc && hasMag) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mGravity = highPass(mGravity, event.values.clone());
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic = highPass(mGeomagnetic,  event.values.clone());
            }
            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                    if (previousAzu == null) {
                        previousAzu = azimut;
                    } else {
//                        final float diff = azimut - previousAzu;
                        updateDataPos(azimut);
                        previousAzu = azimut;
                    }

                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public void onLocationChanged(Location loc) {
        this.location = loc;
        updatePosition();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class aDataPoint {
        public int x;
        public double dist;
        public int id;
        public String name;
        public String thumbnail;
        public aDataPoint(int dx, double dst, int did, String dname, String thumb) {
            this.x = dx;
            this.dist = dst;
            this.id = did;
            this.name = dname;
            this.thumbnail = thumb;
        }
    }

    private class MapFilterLoader extends AsyncTask<Void, List<aDataPoint>, List<aDataPoint>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ShowLoadingDialog();
        }

        @Override
        protected List<aDataPoint> doInBackground(Void... params) {
            return processTrailsData(getTrails());
        }

        @Override
        protected void onPostExecute(List<aDataPoint> result) {
            super.onPostExecute(result);
            if (result != null) {
                drawInitialData();
            }
            if (LoadingDialog != null && LoadingDialog.isShowing()) {
                LoadingDialog.dismiss();
            }
        }
    }
}
