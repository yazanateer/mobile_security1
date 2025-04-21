package com.example.yazecurity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.Manifest;
import android.content.pm.PackageManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;




public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] gravity;
    private float[] geomagnetic;
    private float currentAzimuth = -1f;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isInTelAviv = false;

    private Sensor lightSensor;
    private float currentLux = -1f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.RECORD_AUDIO
                },
                100
        );
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocation();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);


        Button loginButton = findViewById(R.id.login_button);
        EditText passwordField = findViewById(R.id.password);

        loginButton.setOnClickListener(v -> {
            String input = passwordField.getText().toString().trim();

            int batteryLevel = getBatteryLevel();
            int brightnessPercent = getScreenBrightnessPercent();
            boolean facingSouth = currentAzimuth >= 170 && currentAzimuth <= 190;
            float noiseLevel = measureNoiseLevel();
            boolean isLoudEnough = noiseLevel > 50.0f;
            boolean isDarkEnough = currentLux <= 50;

            // Logs
            Log.d("YAZ", "Battery Level: " + batteryLevel + "%");
            Log.d("YAZ", "Brightness %: " + brightnessPercent + "%");
            Log.d("YAZ", "Azimuth: " + currentAzimuth + "°");
            Log.d("YAZ", "Is Facing South: " + facingSouth);
            Log.d("YAZ", "Is in Tel Aviv: " + isInTelAviv);
            Log.d("YAZ", "Noise Level: " + noiseLevel + " dB");
            Log.d("YAZ", "Is Loud Enough: " + isLoudEnough);
            Log.d("YAZ", "Ambient Light: " + currentLux + " lux");
            Log.d("YAZ", "Is Dark Enough: " + isDarkEnough);

            if (input.equals(String.valueOf(batteryLevel)) &&
                    batteryLevel == brightnessPercent &&
                    facingSouth &&
                    isInTelAviv &&
                    isLoudEnough &&
                    isDarkEnough) {

                Toast.makeText(this, "All 7 checks passed ", Toast.LENGTH_LONG).show();
                View dialogView = getLayoutInflater().inflate(R.layout.success_login, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(dialogView);
                builder.setCancelable(true); // optional: let user dismiss

                AlertDialog successDialog = builder.create();
                successDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                successDialog.show();
            } else {
                Toast.makeText(this, "Access denied \nOne or more conditions failed", Toast.LENGTH_SHORT).show();
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_UI);
    }

    private int getBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        if (level == -1 || scale == -1) return 50;
        return (int) ((level / (float) scale) * 100);
    }

    private int getScreenBrightnessPercent() {
        try {
            int brightness = android.provider.Settings.System.getInt(
                    getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS
            );
            return (int) ((brightness / 255.0) * 100);
        } catch (android.provider.Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                gravity = event.values;

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                geomagnetic = event.values;

            if (event.sensor.getType() == Sensor.TYPE_LIGHT)
                currentLux = event.values[0];

            if (gravity != null && geomagnetic != null) {
                float[] R = new float[9];
                float[] I = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
                if (success) {
                    float[] orientation = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    currentAzimuth = (float) Math.toDegrees(orientation[0]);
                    if (currentAzimuth < 0) currentAzimuth += 360;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    };


private void checkLocation() {
    Log.d("YAZ", "Running checkLocation()...");

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        Log.d("YAZ", "Location permission not granted yet.");
        return;
    }

    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener(location -> {
                if (location != null) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    double telAvivLat = 32.0853;
                    double telAvivLon = 34.7818;

                    float[] results = new float[1];
                    Location.distanceBetween(lat, lon, telAvivLat, telAvivLon, results);

                    float distanceInMeters = results[0];
                    isInTelAviv = distanceInMeters <= 10000;

                    Log.d("YAZ", "User Location: " + lat + ", " + lon);
                    Log.d("YAZ", "isInTelAviv: " + isInTelAviv);
                } else {
                    Log.d("YAZ", "Location is NULL");
                }
            });
}

    private float measureNoiseLevel() {
        boolean isEmulator = Build.FINGERPRINT.contains("generic")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for")
                || Build.PRODUCT.contains("sdk")
                || Build.HARDWARE.contains("goldfish")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic");

        if (isEmulator) {
            return 65.0f;
        }

        MediaRecorder recorder = new MediaRecorder();
        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile("/dev/null");
            recorder.prepare();
            recorder.start();

            Thread.sleep(1000);

            int amplitude = recorder.getMaxAmplitude();
            float db = (float) (20 * Math.log10((double) Math.max(amplitude, 1)));

            Log.d("YAZ", "Mic amplitude: " + amplitude + " → dB: " + db);

            return db;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            try {
                recorder.stop();
            } catch (Exception e) {
                Log.d("YAZ", "Recorder.stop() failed (invalid state): " + e.getMessage());
            }
            recorder.release();
        }
    }

}