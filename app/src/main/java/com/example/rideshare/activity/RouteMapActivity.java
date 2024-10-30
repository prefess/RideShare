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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.example.rideshare.R;
import com.example.rideshare.utils.AndroidUtil;


public class RouteMapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private WebView webViewMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_map);

        webViewMap = findViewById(R.id.webViewMap2);

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

        webViewMap.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");

        webViewMap.loadUrl("file:///android_asset/map_view.html");

        webViewMap.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Retrieve the route data passed from the previous activity
                Intent intent = getIntent();
                String origin = intent.getStringExtra("origin");
                String destination = intent.getStringExtra("destination");
                String stops = intent.getStringExtra("stops"); // Stops as a "|" separated string

                String javascript = "javascript:setRouteData('" + origin + "','" + destination + "','" + stops + "')";
                Log.d("RouteMapActivity", "Executing JavaScript: " + javascript);
                //Toast.makeText(RouteMapActivity.this, "Executing JavaScript", Toast.LENGTH_SHORT).show();
                // Call the JavaScript function
                webViewMap.loadUrl(javascript);
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
    private class WebAppInterface {
        @JavascriptInterface
        public void showDistance(double distance) {
            runOnUiThread(() -> {
                // Show the distance as a toast or use it in your app
                Toast.makeText(RouteMapActivity.this, "Distance: " + distance + " km", Toast.LENGTH_LONG).show();
            });
        }
    }
}