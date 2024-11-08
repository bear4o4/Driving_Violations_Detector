package com.example.drivinghabitdetector;
import com.example.drivinghabitdetector.Violation;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class TripDetailScreen extends AppCompatActivity {

    private LinearLayout violationsContainer;
    private String filename = "trip.txt";
    private HashMap<String, List<String>> tripViolations;
    private int tripNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail_screen);

        TextView tripNumberTextView = findViewById(R.id.textViewtripnumber);
        tripNumberTextView.setFontFeatureSettings("fontFeatureSettings='smcp'");
        Button backButton = findViewById(R.id.button2back);
        backButton.setBackgroundColor(Color.parseColor("#D8E1E6"));
        violationsContainer = findViewById(R.id.linearlayputtripdetail);
        tripViolations = new HashMap<>();

        tripNumber = getIntent().getIntExtra("tripNumber", 0);
        tripNumberTextView.setText(String.format(Locale.getDefault(), "TRIP %d", tripNumber));

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(TripDetailScreen.this, TripsScreen.class);
            startActivity(intent);
            finish();
        });

        AdView mAdView = findViewById(R.id.adView4);
        mAdView.setAdSize(AdSize.BANNER);
        mAdView.setAdUnitId("ca-app-pub-7361778772201584~9719775675");
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        readTripViolations();
        displayViolations();
    }

    private void readTripViolations() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput(filename)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Log.d("FileLine", line);
                String[] parts = line.split(",");
                String tripCount = null;
                for (String part : parts) {
                    if (part.startsWith("TripCount:")) {
                        tripCount = part.split(":")[1].trim();
                    }
                }
                if (tripCount != null) {
                    if (!line.contains("Trip started")) {
                        tripViolations.putIfAbsent(tripCount, new ArrayList<>());
                        tripViolations.get(tripCount).add(line);
                    } else {
                        Log.d("TripStartedCheck", "Skipping 'Trip started' line: " + line);
                    }
                } else {
                    Log.e("ViolationError", "Skipping invalid violation line: " + line);
                }
            }
        } catch (IOException e) {
            Log.e("ReadFileError", "Error reading file: " + e.getMessage());
        }
    }

    private void displayViolations() {
        List<String> violations = tripViolations.get(String.valueOf(tripNumber));
        if (violations != null && !violations.isEmpty()) {
            for (String violation : violations) {
                try {
                    if (violation.contains("Trip started")) {
                        Log.d("TripStartedCheck", "Skipping 'Trip started' line: " + violation);
                        continue;
                    }


                    String[] parts = violation.split(",");
                    if (parts.length < 5 || !parts[1].contains("Date:")) {
                        Log.e("ViolationError", "Skipping invalid violation line: " + violation);
                        continue;
                    }

                    String rawText = parts[1].substring(parts[1].indexOf("Text:") + 6).trim();
                    Log.d("ParsedText", "Raw Text: " + rawText);

                    String violationDateTime;
                    String violationDescription;

                    if (rawText.contains("TripCount:")) {
                        String[] textParts = rawText.split("-", 2);
                        violationDateTime = textParts[0].trim();
                        violationDescription = textParts[1].trim();
                    } else {
                        violationDateTime = rawText;
                        violationDescription = parts[4];
                    }

                    Log.d("ParsedViolation", "Date/Time: " + violationDateTime);
                    Log.d("ParsedViolation", "Description: " + violationDescription);

                    double latitude = parseDoubleSafely(parts[5].split(":")[1].trim());
                    double longitude = parseDoubleSafely(parts[6].split(":")[1].trim());


                    String formattedText = violationDateTime + " – " + violationDescription;


                    TextView violationTextView = new TextView(this);
                    violationTextView.setText(formattedText);
                    violationsContainer.addView(violationTextView);


                    Button mapButton = new Button(this);
                    mapButton.setBackgroundColor(Color.parseColor("#D8E1E6"));
                    mapButton.setText("Go to Map");
                    mapButton.setOnClickListener(v -> openGoogleMaps(latitude, longitude));
                    violationsContainer.addView(mapButton);
                } catch (Exception e) {
                    Log.e("DisplayViolationsError", "Error displaying violation: " + e.getMessage());
                }
            }
        } else {
            Log.d("DisplayViolations", "No violations found for trip number: " + tripNumber);
        }
    }

    private void openGoogleMaps(double latitude, double longitude) {
        Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
    private void openMap(double latitude, double longitude) {
        try {
            Uri mapUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude);
            Intent intent = new Intent(Intent.ACTION_VIEW, mapUri);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No application available to view the map.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("OpenMapError", "Error opening map: " + e.getMessage());
            Toast.makeText(this, "Unable to open location. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
    private void openMapWithUrl(double latitude, double longitude) {
        try {
            URL mapUrl = new URL("https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl.toString()));
            startActivity(intent);
        } catch (MalformedURLException e) {
            Log.e("OpenMapUrlError", "Invalid map URL: " + e.getMessage());
            Toast.makeText(this, "Invalid map URL.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("OpenMapError", "Error opening map: " + e.getMessage());
            Toast.makeText(this, "Unable to open location. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
    private double parseDoubleSafely(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            Log.e("ParseError", "Invalid double value: " + value);
            return 0;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        AdView mAdView = findViewById(R.id.adView4);
        if (mAdView != null) {
            mAdView.destroy();
        }
    }

}