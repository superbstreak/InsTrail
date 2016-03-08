package nwhack.instrail.com.instrail;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class DevTest extends Activity implements SensorEventListener {

    private ImageView img;
    private TextView g0;
    private TextView g1;
    private TextView g2;

    private TextView a0;
    private TextView a1;
    private TextView a2;

    private TextView m0;
    private TextView m1;
    private TextView m2;

    private SensorManager mSensorManager;
    private Sensor mGyroSensor;
    private Sensor mAcc;
    private Sensor mMag;

    private float x = 0;
    private float y = 0;
    private float z = 0;
    private float timestamp;

    @Override
    protected void onCreate
            (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_test);
        img = (ImageView) this.findViewById(R.id.dev_img1);
        g0 = (TextView) this.findViewById(R.id.gyro_0);
        g1 = (TextView) this.findViewById(R.id.gyro_1);
        g2 = (TextView) this.findViewById(R.id.gyro_2);

        a0 = (TextView) this.findViewById(R.id.acc_0);
        a1 = (TextView) this.findViewById(R.id.acc_1);
        a2 = (TextView) this.findViewById(R.id.acc_2);

        m0 = (TextView) this.findViewById(R.id.mag_0);
        m1 = (TextView) this.findViewById(R.id.mag_1);
        m2 = (TextView) this.findViewById(R.id.mag_2);

        Log.d("DEV", "CREATED");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorManager.registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mGyroSensor);
        mSensorManager.unregisterListener(this, mAcc);
        mSensorManager.unregisterListener(this, mMag);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (timestamp != 0) {
            if (event.sensor.equals(mGyroSensor)) {
                float dx = event.values[0] - x;
                float dy = event.values[1] - y;
                float dz = event.values[2] - z;
                g0.setText("GYRO dx: "+dx);
                g1.setText("GYRO dy: "+dy);
                g2.setText("GYRO dz: "+dz);
                img.setX(img.getX() + dy*1000);
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("DEV", "ACCURACY DELTA "+sensor.getName()+" del: "+accuracy);

    }
}
