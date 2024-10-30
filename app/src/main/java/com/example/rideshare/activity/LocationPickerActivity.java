package com.example.rideshare.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rideshare.R;
import com.example.rideshare.utils.AndroidUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private WebView webViewMap;
    private String selectedAddress = ""; // To store the selected address
    private double selectedLatitude = 0.0;  // To store the selected latitude
    private double selectedLongitude = 0.0;  // To store the selected longitude

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        webViewMap = findViewById(R.id.webViewMap);
        Button btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Enable JavaScript and other necessary settings in WebView
        webViewMap.getSettings().setJavaScriptEnabled(true);
        webViewMap.getSettings().setDomStorageEnabled(true);
        webViewMap.getSettings().setGeolocationEnabled(true); // Enable geolocation
        webViewMap.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        // Add a JavaScript interface to capture the address
        webViewMap.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");

        // Load Goong Maps and inject JavaScript to handle address selection
        webViewMap.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                webViewMap.loadUrl("javascript:(function() {" +
                        "var searchBox = document.querySelector('.search_input');" +
                        "if (searchBox) {" +
                        "    var lastValue = searchBox.value;" +
                        "    searchBox.addEventListener('change', function() {" +
                        "        var newValue = searchBox.value;" +
                        "        if (newValue !== lastValue) {" +
                        "            lastValue = newValue;" +
                        "            console.log('Selected Address: ' + newValue);" +
//                        "            AndroidInterface.setAddress(newValue);" +
                        "        }" +
                        "    });" +
                        "}" +
                        "})();");

                // Now set the map location
                getDeviceLocation();
            }
        });

        webViewMap.loadUrl("https://maps.goong.io/");
//        webViewMap.loadUrl("file:///android_asset/map_view.html");
        // Confirm Location button click
        btnConfirmLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webViewMap.loadUrl("javascript:(function() { " +
                        "var searchInputValue = document.querySelector('.search_input').value;" +
                        "AndroidInterface.setAddress(searchInputValue);" +
                        "})()");
                if (!selectedAddress.isEmpty() && selectedLatitude!=0 && selectedLongitude!=0) {
                    // Return the selected address to BookARideFragment
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("selectedAddress", selectedAddress);
                    resultIntent.putExtra("latitude", selectedLatitude);
                    resultIntent.putExtra("longitude", selectedLongitude);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    // Handle case where no address was selected or input
//                    AndroidUtil.showToast(LocationPickerActivity.this, "Cannot access");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocation();
            } else {
                // Handle the case where permission is denied
                AndroidUtil.showToast(this, "Location permission is required to use this feature.");
            }
        }
    }

    private void getDeviceLocation() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
//                            setMapLocation(latitude, longitude);
                            // Perform reverse geocoding
                            getAddressFromLocation(latitude, longitude);
                        } else {
                            // Handle the case where location is null
                            AndroidUtil.showToast(this, "Unable to get location. Please try again.");
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void setMapLocation(double latitude, double longitude, String address) {
        String javascript = "javascript:(function() {" +
                "goongjs.accessToken = 'WQCQ0CEATBAYxVd2qAOU7l8t8GK8OQivSX45dMg7';" + // Set your Goong access token here
                "var map = new goongjs.Map({" +
                "    container: 'map', " +
                "    style: 'https://tiles.goong.io/assets/goong_map_dark.json', " +
                "    center: [" + longitude + ", " + latitude + "], " + // Use the provided latitude and longitude
                "    zoom: 14" +
                "});" +
//                "document.getElementById('rc_select_0').value = '" + address + "';" +
                "})();";
        webViewMap.loadUrl(javascript); // Fill the input field);
    }


    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);  // Get the full address
                setMapLocation(latitude, longitude, address);
            } else {
                AndroidUtil.showToast(this, "Unable to get address. Please try again.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            AndroidUtil.showToast(this, "Geocoder service not available.");
        }
    }

    // JavaScript Interface class to capture data from WebView
    private class WebAppInterface {

        @JavascriptInterface
        public void setAddress(String address) {
            selectedAddress = address;  // This method gets called from the injected JavaScript
            Log.d("LocationPickerActivity", "Address set: " + address); // Log to confirm it's called

            getLatLngFromAddress(address);
        }
    }

    private void getLatLngFromAddress(String address) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                selectedLatitude = addresses.get(0).getLatitude();
                selectedLongitude = addresses.get(0).getLongitude();
            } else {
                AndroidUtil.showToast(this, "Unable to get coordinates for the address. Please try again.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            AndroidUtil.showToast(this, "Geocoder service not available.");
        }
    }

}