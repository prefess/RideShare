package com.example.rideshare.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.rideshare.R;
import com.example.rideshare.utils.AndroidUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TrackDriverActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private WebView webViewMap;

    private double driverLat, driverLon;

    private boolean isMapLoaded = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_driver);

        webViewMap = findViewById(R.id.webViewMap3);


        Intent intent = getIntent();
        String rideId = intent.getStringExtra("rideId");
        DatabaseReference rideLocation = FirebaseDatabase.getInstance().getReference().child("Rides").child(rideId).child("driverLocation");
        rideLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                    driverLat = snapshot.child("latitude").getValue(Double.class);
                    driverLon = snapshot.child("longitude").getValue(Double.class);

                if (isMapLoaded) {
                    updateDriverLocation(driverLat, driverLon);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        // Check location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
//            getDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }


        webViewMap.getSettings().setJavaScriptEnabled(true);
        webViewMap.getSettings().setDomStorageEnabled(true);
        webViewMap.getSettings().setGeolocationEnabled(true); // Enable geolocation
        webViewMap.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

//        webViewMap.addJavascriptInterface(new TrackDriverActivity.WebAppInterface(), "AndroidInterface");

        webViewMap.loadUrl("file:///android_asset/map_view_driver_location_marker.html");

        webViewMap.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

                isMapLoaded = true;
                // Retrieve the route data passed from the previous activity
                Intent intent = getIntent();
                String origin = intent.getStringExtra("origin");
                String destination = intent.getStringExtra("destination");
                String stops = intent.getStringExtra("stops"); // Stops as a "|" separated string

                String javascript = "javascript:setRouteData('" + origin + "','" + destination + "','" + stops + "')";
                Log.d("TrackDriverActivity", "Executing JavaScript: " + javascript);
                Toast.makeText(TrackDriverActivity.this, "Executing JavaScript", Toast.LENGTH_SHORT).show();
                // Call the JavaScript function
                webViewMap.loadUrl(javascript);
                showDriverLocationMarker(driverLat, driverLon);
            }
        });
        // Load the map HTML file


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getDeviceLocation();
            } else {
                // Handle the case where permission is denied
                AndroidUtil.showToast(this, "Location permission is required to use this feature.");
            }
        }
    }

    private void showDriverLocationMarker(double latitude, double longitude) {
        // Convert latitude and longitude to a string format
        String driverLocation = latitude + "," + longitude;
        String javascript = "javascript:addDriverMarker('" + driverLocation + "')";

        Log.d("TrackDriverActivity", "Executing JavaScript to add driver marker: " + javascript);
        webViewMap.loadUrl(javascript);
    }
    @JavascriptInterface
    public void updateDriverLocation(double latitude, double longitude) {
        runOnUiThread(() -> {
            showDriverLocationMarker(latitude, longitude);
        });
    }
}