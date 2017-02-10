package appinventor.ai_bengg.WMUC_Radio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import appinventor.ai_bengg.WMUC_Radio.R;

public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Thread myThread = new Thread(){
            public void run() {
                try {
                    sleep(2000);
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                    finish();
                }catch (InterruptedException e){

                }
            }
        };
        myThread.start();
    }
}