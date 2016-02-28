package nwhack.instrail.com.instrail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;


public class Splash extends Activity {
    int delay = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        final Intent i = new Intent(Splash.this, MainActivity.class);

        new CountDownTimer(delay, 1000) //1.5 second Timer
        {
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                startActivity(i);
                finish();
            }
        }.start();

    }
}
