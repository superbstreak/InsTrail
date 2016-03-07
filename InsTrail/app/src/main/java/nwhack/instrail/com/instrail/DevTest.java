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

    private SensorManager mSensorManager;
    private Sensor mGyroSensor;

    @Override
    protected void onCreate
            (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_test);
        img = (ImageView) this.findViewById(R.id.dev_img1);
        g0 = (TextView) this.findViewById(R.id.gyro_0);
        g1 = (TextView) this.findViewById(R.id.gyro_1);
        g2 = (TextView) this.findViewById(R.id.gyro_2);

        Log.d("DEV", "CREATED");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float buff = 10;
        // x horizontal
        // y vertical
        // z out
        g0.setText("GYRO x: "+event.values[0]);
        g1.setText("GYRO y: "+event.values[1]);
        g2.setText("GYRO z: "+event.values[2]);
        

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("DEV", "ACCURACY DELTA "+sensor.getName()+" del: "+accuracy);

    }
}
