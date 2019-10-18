package petelap.shakelight;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private int ShakeCount;
    private int CountDownSubtract;
    private boolean bLight;
    private FloatingActionButton fab;
    private TextView btnText;
    private CircleProgressBar circleProgressBar;
    private CameraManager cameraManager;
    private Runnable runnable;
    private Handler handler;
    private final int THREAD_TIME_MS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SharedPreferences
        SharedPreferences pref = getApplicationContext().getSharedPreferences("appPref", Context.MODE_PRIVATE);
        float shakeThresholdGravity = pref.getFloat("shakeThresholdGravity", 2.0f);
        int shakeSlopTime =  pref.getInt("shakeSlopTime", 50);
        int shakeCountResetTime = pref.getInt("shakeCountResetTime", 500);
        int countDown = pref.getInt("countdown", 3000);

        ShakeDetector.setShakeThresholdGravity(shakeThresholdGravity);
        ShakeDetector.setShakeSlopTime(shakeSlopTime);
        ShakeDetector.setShakeCountResetTime(shakeCountResetTime);

        ShakeCount = pref.getInt("shakes", 0);
        CountDownSubtract = (int)(( (float)THREAD_TIME_MS / (countDown / (float)THREAD_TIME_MS ) ) *-1 );

        bLight = pref.getBoolean("lightonoff",false);

        circleProgressBar = findViewById(R.id.custom_progressBar);
        circleProgressBar.setStrokeWidth(50f);
        circleProgressBar.setProgressWithAnimation(ShakeCount, 100);

        fab = findViewById(R.id.button);
        btnText = findViewById(R.id.btnText);

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Light on/off
                if (!bLight) {
                    bLight = true;
                    btnText.setText(R.string.light_on);
                    fab.setBackgroundTintList(ColorStateList.valueOf(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null)));
                } else {
                    bLight = false;
                    btnText.setText(R.string.light_off);
                    fab.setBackgroundTintList(ColorStateList.valueOf(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null)));
                }
            }
        });

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert mSensorManager != null;
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                handleShakeEvent(count);
            }
        });

        // light on power draw loop
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, THREAD_TIME_MS);
                if (bLight) {
                    if (ShakeCount > 0) {
                        flashLightToggle(true);
                        handleShakeEvent(CountDownSubtract);
                    }
                    if (ShakeCount == 0) {
                        flashLightToggle(false);
                    }
                } else {
                    flashLightToggle(false);
                }
            }
        };
        handler.postDelayed(runnable, THREAD_TIME_MS);
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
        if (id == R.id.navigation_settings) {
            // Settings
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        // unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        handler.removeCallbacksAndMessages(null);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("appPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("shakes", ShakeCount);
        editor.putBoolean("lightonoff", bLight);
        editor.commit();
        flashLightToggle(false);

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
        circleProgressBar.setProgressWithAnimation(ShakeCount, 100);
        flashLightToggle(bLight);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, THREAD_TIME_MS);
                if (bLight) {
                    if (ShakeCount > 0) {
                        flashLightToggle(true);
                        handleShakeEvent(CountDownSubtract);
                    }
                    if (ShakeCount == 0) {
                        flashLightToggle(false);
                    }
                } else {
                    flashLightToggle(false);
                }
            }
        };
        handler.postDelayed(runnable, THREAD_TIME_MS);
    }

    private void handleShakeEvent(int count) {
        ShakeCount += count;
        if (ShakeCount > 100) {
            ShakeCount = 100;
        }
        if (ShakeCount < 0) {
            ShakeCount = 0;
        }
        circleProgressBar.setProgressWithAnimation(ShakeCount, 100);
    }

    public void flashLightToggle(boolean bOnOff) {
        // bOnOff True = Light on. bOnOff False = Light off
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String CamID = null;
            try {
                for (String camID : cameraManager.getCameraIdList()) {
                    CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(camID);
                    int lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    if (lensFacing == CameraCharacteristics.LENS_FACING_BACK && cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                        CamID = camID;
                        break;
                    } else if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT && cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                        CamID = camID;
                    }
                }
                if (CamID != null) {
                    cameraManager.setTorchMode(CamID, bOnOff);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
                Toast.makeText(this,"Sorry there was a camera error!",Toast.LENGTH_SHORT).show();
            }
        }
    }
}