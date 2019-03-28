package petelap.shakelight;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar seekBarGravity;
    private SeekBar seekBarShake;
    private SeekBar seekBarReset;
    private SeekBar seekBarCountDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button btnSave = findViewById(R.id.btnSave);
        Button btnReset = findViewById(R.id.btnReset);
        seekBarGravity = findViewById(R.id.gravity_seekBar);
        seekBarShake = findViewById(R.id.shake_seekBar);
        seekBarReset = findViewById(R.id.reset_seekBar);
        seekBarCountDown = findViewById(R.id.countdown_seekBar);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("appPref", Context.MODE_PRIVATE);
        float shakeThresholdGravity = pref.getFloat("shakeThresholdGravity", 2.0f) * 100f;
        int shakeSlopTime =  pref.getInt("shakeSlopTime", 50);
        int shakeCountResetTime = pref.getInt("shakeCountResetTime", 500);
        int countDown = pref.getInt("countdown",3000);

        seekBarGravity.setProgress((int)shakeThresholdGravity);
        seekBarShake.setProgress(shakeSlopTime);
        seekBarReset.setProgress(shakeCountResetTime);
        seekBarCountDown.setProgress(countDown);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float shakeThresholdGravity = seekBarGravity.getProgress() / 100f;
                int shakeSlopTime = seekBarShake.getProgress();
                int shakeCountResetTime = seekBarReset.getProgress();
                int countDown = seekBarCountDown.getProgress();

                SharedPreferences pref = getApplicationContext().getSharedPreferences("appPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putFloat("shakeThresholdGravity", shakeThresholdGravity);
                editor.putInt("shakeSlopTime", shakeSlopTime);
                editor.putInt("shakeCountResetTime", shakeCountResetTime);
                editor.putInt("countdown", countDown);
                editor.commit();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float shakeThresholdGravity = 2.0f;
                int shakeSlopTime = 50;
                int shakeCountResetTime = 500;
                int countDown = 3000;

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    seekBarGravity.setProgress((int)(shakeThresholdGravity * 100f),true);
                    seekBarShake.setProgress(shakeSlopTime, true);
                    seekBarReset.setProgress(shakeCountResetTime, true);
                    seekBarCountDown.setProgress(countDown,true);
                } else{
                    seekBarGravity.setProgress((int)(shakeThresholdGravity * 100f));
                    seekBarShake.setProgress(shakeSlopTime);
                    seekBarReset.setProgress(shakeCountResetTime);
                    seekBarCountDown.setProgress(countDown);
                }

                SharedPreferences pref = getApplicationContext().getSharedPreferences("appPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putFloat("shakeThresholdGravity", shakeThresholdGravity);
                editor.putInt("shakeSlopTime", shakeSlopTime);
                editor.putInt("shakeCountResetTime", shakeCountResetTime);
                editor.putInt("countdown", countDown);
                editor.commit();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.navigation_main) {
            // Settings
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}