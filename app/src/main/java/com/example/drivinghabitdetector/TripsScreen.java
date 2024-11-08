package com.example.drivinghabitdetector;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TripsScreen extends AppCompatActivity {
    LinearLayout tripsContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips_screen);

        Button bq = findViewById(R.id.buttontripscreenback);
        bq.setBackgroundColor(Color.parseColor("#D8E1E6"));
        tripsContainer = findViewById(R.id.linearlayoutinsidescrollview);
        bq.setOnClickListener(v -> {
            Intent i = new Intent(TripsScreen.this, HomeScreen.class);
            startActivity(i);
        });

        AdView adView = findViewById(R.id.adView3);
        //adView.setAdSize(AdSize.BANNER);
        //adView.setAdUnitId("ca-app-pub-7361778772201584~9719775675");
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        display();
    }

    private void display() {
        List<String> trips = loadTrips();
        Map<Integer, String> uniqueTrips = new LinkedHashMap<>();

        for (String trip : trips) {
            String[] parts = trip.split(",");
            String tripDate = "";
            String tripNumberStr = "";

            for (String part : parts) {
                if (part.startsWith("TripCount:")) {
                    tripNumberStr = part.split(":")[1].trim();
                } else if (part.startsWith("Date:")) {
                    tripDate = part.split(":")[1].trim();
                }
            }

            int tripNumber;
            try {
                tripNumber = Integer.parseInt(tripNumberStr);
            } catch (NumberFormatException e) {
                Log.e("TripsScreen", "Invalid trip number: " + tripNumberStr);
                continue;
            }

            uniqueTrips.put(tripNumber, "Trip " + tripNumber + " - " + tripDate);
            //uniqueTrips.setFontFeatureSettings("fontFeatureSettings='smcp'");
        }

        for (Map.Entry<Integer, String> entry : uniqueTrips.entrySet()) {
            Button tripButton = new Button(this);
            tripButton.setFontFeatureSettings("fontFeatureSettings='smcp'");
            tripButton.setBackgroundColor(Color.parseColor("#D8E1E6"));
            tripButton.setText(entry.getValue());
            tripButton.setOnClickListener(v -> openTripDetail(entry.getKey()));
            tripsContainer.addView(tripButton);
        }
    }

    private List<String> loadTrips() {
        List<String> trips = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput("trip.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("TripCount:")) {
                    trips.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trips;
    }
    private void openTripDetail(int tripNumber) {
        Intent intent = new Intent(TripsScreen.this, TripDetailScreen.class);
        intent.putExtra("tripNumber", tripNumber);
        startActivity(intent);
    }

}