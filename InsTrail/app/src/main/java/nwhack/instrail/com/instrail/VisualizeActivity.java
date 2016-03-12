package nwhack.instrail.com.instrail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Utils.GPSTracker;
import nwhack.instrail.com.instrail.Model.Trail;

@SuppressWarnings("deprecation")
public class VisualizeActivity extends BaseActivity implements SensorEventListener, SurfaceHolder.Callback {

    private Activity mContext;
    private RelativeLayout freeDraw;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Camera acam = null;
    private boolean inPreview = false;

    private SensorManager mSensorManager;
    private Sensor mGyroSensor;
    private Sensor mAcc;
    private Sensor mMag;
    private GPSTracker gps;

    private final int MAX_DIST = 250; //km
    private double currentLAT;
    private double currentLON;
    private boolean hasGyroscope = false;
    private boolean hasAcc = false;
    private boolean hasMag = false;
    private final float PI = 3.1415926f;
    private boolean isTracking;
    private boolean isCameraOn;
    private float timestamp;
    private int[] device = {0,0};
    float[] mGravity;
    float[] mGeomagnetic;
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
        freeDraw = (RelativeLayout) this.findViewById(R.id.visualize_free_draw);
        mSurfaceView = (SurfaceView) findViewById(R.id.visualize_surface_camera);
        mContext = this;
        isTracking = false;
        isCameraOn = true;
        updatePosition();
        setUpSensor();
        registerSensor();
        device[0] = BaseActivity.getDeviceMetric()[0];
        device[1] = BaseActivity.getDeviceMetric()[1];
        Log.e("VIS", "METRIC "+device[0]+"  "+device[1]);
        data = processTrailsData(BaseActivity.trails);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

    }

    // ============================================================================================

    private List processTrailsData(List<Trail> trails) {
        data = new ArrayList<>();
        if (trails != null) {
            int size = trails.size();
            for (int i =0; i < size; i++) {
                Trail tr = trails.get(i);
                final String name = tr.getName();
                final String thumb = tr.getThumbnail();
                final double lat = tr.getLat();
                final double lon = tr.getLon();
                Log.e("VIS", "ID: "+i+" NAME: "+name);
                aDataPoint pt = new aDataPoint(calculateDefaultOffset(
                        currentLAT, currentLON, lat,lon),
                        calculateDistance(currentLAT,currentLON,lat,lon),
                        i,name, thumb);
                data.add(pt);
            }
            drawInitialData();
        }
        return data;
    }

    private void updatePosition() {
        if (gps == null) {
            gps = new GPSTracker(this);
        }
        // need to be able to update user location whne changed
        currentLAT = 49.2611;
        currentLON = -123.2531;
    }

    private void drawInitialData() {
        if (data != null && freeDraw != null){
            texts = new ArrayList<>();
            int size = data.size();
            int deviceHeightHalf = device[1]/2;
            Log.e("MAX?", ""+maxDist);
            for (int i = 0 ; i < size; i ++) {
                aDataPoint point = data.get(i);
                if (point.dist > MAX_DIST) {

                } else {
                    View view = getLayoutInflater().inflate(R.layout.visualize_location, null);
                    final ImageView valueTV = (ImageView) view.findViewById(R.id.vis_loc_img);
                    final TextView title = (TextView) view.findViewById(R.id.vis_loc_txtTitle);
                    final TextView dists = (TextView) view.findViewById(R.id.vis_loc_txtDistance);
                    title.setText(point.name+"");
                    dists.setText((int) point.dist + " km");
                    view.setX(point.x);
                    view.setY((int)(deviceHeightHalf*(1- (point.dist/maxDist))));
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
                                Log.e("VIS", "CLICK ID: "+v.getTag());
                                intent.putExtra(Constant.TRAIL_POSITION_TAG, (int)v.getTag());
                                startActivity(intent);
                            } catch (Exception e) {

                            }
                        }
                    });
                    texts.add(view);
                    ((RelativeLayout) freeDraw).addView(view);
                }
            }
        }
    }

    private double calculateDistance(final double lat1, final double lon1, final double lat2, final double lon2) {
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.pow(Math.sin(dlat/2),2) + Math.cos(lat1) * Math.cos(lat2) * (Math.sin(dlon/2)),2);
        double c = 2 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        double d = 6371 * c;
        if (d < MAX_DIST && maxDist < d) {
            maxDist = d;
        }
        return d;
    }

    private void updateDataPos(final float azu) {
        if (data != null && texts != null){
            for (int i = 0; i < texts.size(); i++) {
                    View currentView = texts.get(i);
                    final float currentTVX = currentView.getX();
                    float newTVX = data.get((int)currentView.getTag()).x - azu*device[0];
                    final float diff = Math.abs(currentTVX - newTVX);
                    if (diff > device[0]/13) {
                        currentView.setX(newTVX);
                    }
            }
        }
    }

    private int calculateDefaultOffset(double currentlat, double currentlon, double lat, double lon) {
        // θ = atan2(sin(Δlong)*cos(lat2), cos(lat1)*sin(lat2) − sin(lat1)*cos(lat2)*cos(Δlong))
        final double deltaLon = lon - currentlon;
        final double sinDeltaLon = Math.sin(deltaLon);
        final double cosLat = Math.cos(lat);
        final double cosLat1sinLat2 = Math.cos(currentlat)*Math.sin(lat);
        final double sinLat1cosLat2cosDeltaLon = Math.sin(currentlat)*Math.cos(lat)*Math.cos(deltaLon);
        final double theta = Math.atan2(sinDeltaLon*cosLat, cosLat1sinLat2 - sinLat1cosLat2cosDeltaLon);
        return (int)(screenVisCen + theta*device[0]);
    }

    private void unregisterSensor() {
        if (isTracking) {
            mSensorManager.unregisterListener(this, mGyroSensor);
            mSensorManager.unregisterListener(this, mAcc);
            mSensorManager.unregisterListener(this, mMag);
            isTracking = false;
        }
    }

    private void registerSensor() {
        if (!isTracking) {
            mSensorManager.registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_NORMAL);
            isTracking = true;
        }
    }

    private void setUpSensor() {
        getAvailableSensors();
        if (hasGyroscope) {

        } else if (hasAcc && hasMag) {

        }
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    private void getAvailableSensors(){
        PackageManager packageManager = getPackageManager();
        hasGyroscope = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        hasAcc = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        hasMag = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
    }

    // =============================================================================================

    @Override
    public void onResume(){
        super.onResume();
        mContext = this;
        device[0] = BaseActivity.getDeviceMetric()[0];
        device[1] = BaseActivity.getDeviceMetric()[1];
        freeDraw.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        registerSensor();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterSensor();
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
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
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
                    updateDataPos(azimut);
                    previousAzu = azimut;
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
}
