package com.example.drivinghabitdetector;

import static java.util.Locale.getDefault;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeScreen extends AppCompatActivity implements SensorEventListener, LocationListener {
    private LinearLayout tripsContainer;
    private TextView tripcounttextview;
    private SensorManager sensorManager;

    boolean track = false;
    String filename = "trip.txt";
    FileOutputStream fos;
    private float lastacceleration = 0.0f;
    double latitude;
    double longitude;
    private Handler handler = new Handler();
    private boolean violationdetected = true;

    InterstitialAd InAd;
    private AdView mAdView;
    private int tripcount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        tripcount = getSharedPreferences("tripcount", MODE_PRIVATE).getInt("tripcount", 0);

        MobileAds.initialize(this, initializationStatus -> {
        });

        Button stopbutton;
        Button tripsButton;
        Button startButton;

        tripsContainer = findViewById(R.id.LLhomescreen);

        stopbutton = findViewById(R.id.buttonstop);
        stopbutton.setBackgroundColor(Color.parseColor("#D8E1E6"));
        stopbutton.setFontFeatureSettings("fontFeatureSettings='smcp'");
        tripsButton = findViewById(R.id.buttontrips);
        tripsButton.setBackgroundColor(Color.parseColor("#D8E1E6"));
        tripsButton.setFontFeatureSettings("fontFeatureSettings='smcp'");
        startButton = findViewById(R.id.buttonstart);
        startButton.setBackgroundColor(Color.parseColor("#D8E1E6"));
        startButton.setFontFeatureSettings("fontFeatureSettings='smcp'");

        tripcounttextview = findViewById(R.id.textViewtripcountnumber);

        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Sensor defaultGyroscope =sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor defaultAccelerometer =sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SharedPreferences sharedPreferences=getSharedPreferences("tripcount",MODE_PRIVATE);

        mAdView = findViewById(R.id.adView2);
        //mAdView.setAdUnitId("ca-app-pub-7361778772201584~9719775675");
        //mAdView.setAdSize(AdSize.BANNER);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        AdRequest adRequest1 = new AdRequest.Builder().build();
        InterstitialAd.load(this,"ca-app-pub-7361778772201584~9719775675",adRequest1,
                new InterstitialAdLoadCallback(){
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                InAd=interstitialAd;
            }
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                InAd=null;
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location lastKnownLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                latitude = lastKnownLocation.getLatitude();
                longitude = lastKnownLocation.getLongitude();
                Log.d("Location", "Last known location: " + latitude + ", " + longitude);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!track) {
                    sm.registerListener(HomeScreen.this, defaultGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
                    sm.registerListener(HomeScreen.this, defaultAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                    if (ActivityCompat.checkSelfPermission(HomeScreen.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(HomeScreen.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(HomeScreen.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                        return;
                    }
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, HomeScreen.this);

                    tripcount++;

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("tripcount", tripcount);
                    editor.apply();

                    tripcounttextview.setText("TRIP : "+String.valueOf(tripcount));
                    tripcounttextview.setFontFeatureSettings("fontFeatureSettings='smcp'");

                    String tripInfo = "Trip " + tripcount ;
                    //String tripInfo="";
                    //StoreInFile(tripInfo, latitude, longitude);

                    track = true;
                }
            }
        });
        stopbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(track==true){
                   sm.unregisterListener(HomeScreen.this);
                   lm.removeUpdates(HomeScreen.this);
                   track=false;
               }
            }
        });
        tripsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeScreen.this, TripsScreen.class);
                if (InAd != null) {
                    InAd.show(HomeScreen.this);
                }
                startActivity(intent);
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (track==false) {
            return;
        }
        if(violationdetected==true){
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];

                float GETA=(float)Math.sqrt(x*x+y*y);
                float A=10.0f;
                if (GETA> A) {
                    TextView tv = new TextView(this);
                    String nowtime = new SimpleDateFormat("d MMMM, h:mm a", Locale.getDefault()).format(new Date());
                    String Text= nowtime+"-Harsh acceleration ";
                    StoreInFile(Text,latitude,longitude);
                    tv.setText(Text);
                    tripsContainer.addView(tv);
                    setTextSizeForAllTextViews(tripsContainer, 9);
                    violationdetected=false;

                    handler.postDelayed(()->{
                        violationdetected=true;
                    },6000);
                }
                float D=-3.0f;
                if (y <= D) {
                    String nowtime = new SimpleDateFormat("d MMMM, h:mm a", Locale.getDefault()).format(new Date());
                    String Text= nowtime+"-Harsh braking ";
                    TextView tv = new TextView(this);
                    StoreInFile(Text,latitude,longitude);
                    tv.setText(Text);
                    tripsContainer.addView(tv);
                    setTextSizeForAllTextViews(tripsContainer, 9);
                    violationdetected=false;
                    handler.postDelayed(()->{
                        violationdetected=true;
                    },6000);
                }
                lastacceleration=GETA;
            }
            else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                float angularSpeedX = sensorEvent.values[0];
                float angularSpeedY = sensorEvent.values[1];
                float getangularspeed=(float)Math.sqrt(angularSpeedX*angularSpeedX+angularSpeedY*angularSpeedY);

                if (getangularspeed > 5.0f && (Math.abs(angularSpeedX)>3||Math.abs(angularSpeedY)>3)) {
                    String nowtime = new SimpleDateFormat("d MMMM, h:mm a", Locale.getDefault()).format(new Date());
                    String Text=nowtime+"-Harsh turning";
                    StoreInFile(Text,latitude,longitude);
                    TextView tv = new TextView(this);
                    tv.setText(Text);
                    tripsContainer.addView(tv);
                    setTextSizeForAllTextViews(tripsContainer, 9);
                    violationdetected=false;
                    handler.postDelayed(()->{
                        violationdetected=true;
                    },6000);
                }
            }
        }
    }
    private void StoreInFile(String text, double latitude, double longitude) {
        try {
                fos = openFileOutput(filename, MODE_APPEND);
                String nowtime = new SimpleDateFormat("d MMMM, h:mm a", Locale.getDefault()).format(new Date());
                String data = "TripCount:" + tripcount + "," + "Date:" + nowtime + "," + "Text:" + text + "," + "Latitude:" + latitude + "," + "Longitude:" + longitude + "\n";
                fos.write(data.getBytes());
                fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (track == false) {
            return;
        }
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.d("LocationUpdate", "Latitude: " + latitude + ", Longitude: " + longitude);
            float speed = location.getSpeed() * 3.6f; // Convert speed to km/h
            if (speed > 60.0f) {
                String nowtime = new SimpleDateFormat("d MMMM, h:mm a", Locale.getDefault()).format(new Date());
                String Text = nowtime + "-Over speeding  ";
                StoreInFile(Text, latitude, longitude);
                TextView tv = new TextView(this);
                tv.setText(Text);
                tripsContainer.addView(tv);
            }
        } else {
            Log.d("LocationUpdate", "Location is null, waiting for a valid update.");
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i){}

    private void setTextSizeForAllTextViews(LinearLayout container, float textSize) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextSize(textSize);
                ((TextView) child).setTextColor(Color.parseColor("#282D37"));
                ((TextView)child).setFontFeatureSettings("fontFeatureSettings='smcp'");
            }
        }
    }

}