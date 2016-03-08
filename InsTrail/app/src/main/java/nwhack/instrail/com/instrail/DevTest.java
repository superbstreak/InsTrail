package nwhack.instrail.com.instrail;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

public class DevTest extends Activity implements SensorEventListener, View.OnClickListener, SurfaceHolder.Callback {

    private ImageView img1;
    private ImageView img2;
    private ImageView img3;
    private ImageView img4;
    private ImageView img5;
    private ImageView img6;
    private ImageView img7;
    private ImageView img8;


    private TextView g0;
    private TextView g1;
    private TextView g2;

    private TextView a0;
    private TextView a1;
    private TextView a2;

    private TextView m0;
    private TextView m1;
    private TextView m2;

    ImageButton camera;
    ImageButton start;
    ImageButton pause;

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Camera acam = null;
    private boolean inPreview = false;

    private SensorManager mSensorManager;
    private Sensor mGyroSensor;
    private Sensor mAcc;
    private Sensor mMag;

    private final float PI = 3.1415926f;
    private boolean isTracking;
    private boolean isCameraOn;
    private float x = 0;
    private float y = 0;
    private float z = 0;
    private float timestamp;
    private int w = 0;
    private int h = 0;
    private int diameterH = 0;
    private int diameterV = 0;
    private float fovX = 0;
    private float fovY = 0;
    private float fovZ = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_test);

        img1 = (ImageView) this.findViewById(R.id.dev_img1);
        img2 = (ImageView) this.findViewById(R.id.dev_img2);
        img3 = (ImageView) this.findViewById(R.id.dev_img3);
        img4 = (ImageView) this.findViewById(R.id.dev_img4);
        img5 = (ImageView) this.findViewById(R.id.dev_img5);
        img6 = (ImageView) this.findViewById(R.id.dev_img6);
        img7 = (ImageView) this.findViewById(R.id.dev_img7);
        img8 = (ImageView) this.findViewById(R.id.dev_img8);

        g0 = (TextView) this.findViewById(R.id.gyro_0);
        g1 = (TextView) this.findViewById(R.id.gyro_1);
        g2 = (TextView) this.findViewById(R.id.gyro_2);

        a0 = (TextView) this.findViewById(R.id.acc_0);
        a1 = (TextView) this.findViewById(R.id.acc_1);
        a2 = (TextView) this.findViewById(R.id.acc_2);

        m0 = (TextView) this.findViewById(R.id.mag_0);
        m1 = (TextView) this.findViewById(R.id.mag_1);
        m2 = (TextView) this.findViewById(R.id.mag_2);

        camera = (ImageButton) this.findViewById(R.id.dev_cam);
        camera.setOnClickListener(this);
        start = (ImageButton) this.findViewById(R.id.dev_start);
        start.setOnClickListener(this);
        pause = (ImageButton) this.findViewById(R.id.dev_pause);
        pause.setOnClickListener(this);
        isTracking = true;
        isCameraOn = true;

        // get screen width
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        w = size.x;
        h = size.y;
        diameterH = w * 8; // assume user circle
        diameterV = h * 4; // assume degree is 90
        initImgPos();

        Log.d("DEV", "CREATED");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//        mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorManager.registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
//        mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_UI);
//        mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_UI);


        mSurfaceView = (SurfaceView) findViewById(R.id.dev_surface_camera);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isTracking) {
            mSensorManager.unregisterListener(this, mGyroSensor);
//            mSensorManager.unregisterListener(this, mAcc);
//            mSensorManager.unregisterListener(this, mMag);
            isTracking = false;
        }
    }

    private void initImgPos() {
        float f = 2;
        img1.setX(0);
        img2.setX(1 * f * w);
        img3.setX(2 * f * w);
        img4.setX(3 * f * w);
        img5.setX(4 * f * w);
        img6.setX(-3 * f * w);
        img7.setX(-2 * f * w);
        img8.setX(-1 * f * w);
    }

    private void updateImgPos(final float dy) {
        final float offset = dy * diameterH;
        img1.setX(img1.getX() + offset);
        img2.setX(img2.getX() + offset);
        img3.setX(img3.getX() + offset);
        img4.setX(img4.getX() + offset);
        img5.setX(img5.getX() + offset);
        img6.setX(img6.getX() + offset);
        img7.setX(img7.getX() + offset);
        img8.setX(img8.getX() + offset);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (timestamp != 0) {
            if (event.sensor.equals(mGyroSensor)) {
                float dx = event.values[0] - x;
                float dy = event.values[1] - y;
                float dz = event.values[2] - z;
                g0.setText("GYRO dx: " + dx);
                g1.setText("GYRO dy: " + dy);
                g2.setText("GYRO FOV-X: " + dy * diameterH + "  IMG1: " + img1.getX());
                updateImgPos(dy);
//                fovX = img1.getX() + dy*diameterH;
//                img1.setX(fovX);     // roll will change in x direction
            }
//            else if (event.sensor.equals(mAcc)){
//                a0.setText("ACC x: " + event.values[0]);
//                a1.setText("ACC y: "+event.values[1]);
//                a2.setText("ACC z: " + event.values[2]);
//            } else if (event.sensor.equals(mMag)){
//                m0.setText("MAG x: " + event.values[0]);
//                m1.setText("MAG y: "+event.values[1]);
//                m2.setText("MAG z: " + event.values[2]);
//            }
        }
        timestamp = event.timestamp;
        if (event.sensor.equals(mGyroSensor)) {
            x = event.values[0]; // pitch
            y = event.values[1]; // yaw
            z = event.values[2]; // roll
        }

    }

    private float min = 0;
    private float max = 0;

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("DEV", "ACCURACY DELTA " + sensor.getName() + " del: " + accuracy);

    }

    @Override
    public void onClick(View v) {
        if (v.equals(camera)) {
            if (isCameraOn) {
                Toast.makeText(getApplicationContext(), "STOP CAMERA", Toast.LENGTH_SHORT).show();
                acam.stopPreview();
            } else {
                Toast.makeText(getApplicationContext(), "START CAMERA", Toast.LENGTH_SHORT).show();
                acam.startPreview();
            }
            isCameraOn = !isCameraOn;
        } else if (v.equals(start)) {
            if (!isTracking) {
                Toast.makeText(getApplicationContext(), "START TRACKING", Toast.LENGTH_SHORT).show();
                mSensorManager.registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
//                mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_UI);
//                mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_UI);
                isTracking = true;
            }
        } else if (v.equals(pause)) {
            if (isTracking) {
                Toast.makeText(getApplicationContext(), "STOP TRACKING", Toast.LENGTH_SHORT).show();
                mSensorManager.unregisterListener(this, mGyroSensor);
//                mSensorManager.unregisterListener(this, mAcc);
//                mSensorManager.unregisterListener(this, mMag);
                isTracking = false;
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(inPreview){
            acam.stopPreview();
            inPreview = false;
        }

        if (camera != null){
            try {
                Camera.Size preSize = getOptimalPreviewSize(acam.getParameters().getSupportedPreviewSizes(),w,h);
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
    public void surfaceCreated(SurfaceHolder holder) {
        acam = Camera.open();
        acam.setDisplayOrientation(90);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        acam.stopPreview();
        acam.release();
        acam = null;
        inPreview = false;
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}