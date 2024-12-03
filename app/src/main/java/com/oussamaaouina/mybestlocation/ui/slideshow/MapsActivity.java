package com.oussamaaouina.mybestlocation.ui.slideshow;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.oussamaaouina.mybestlocation.R;
import com.oussamaaouina.mybestlocation.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {
    private GoogleMap googleMap;
    private ActivityMapsBinding binding;
    private double longitude, latitude;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        longitude = Double.parseDouble(getIntent().getStringExtra("longitude"));
        latitude = Double.parseDouble(getIntent().getStringExtra("latitude"));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnMapClickListener(this);
        googleMap.setOnMarkerClickListener(this);

        LatLng position = new LatLng(latitude, longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 12));  // Adding zoom level

        MarkerOptions options = new MarkerOptions()
                .position(position)
                .title("Save")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

        marker = googleMap.addMarker(options);

        // Enable zoom controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        // When clicking on a place on the map, update the marker's position
        if (marker != null) {
            marker.setPosition(latLng);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Save the marker's position
        LatLng position = marker.getPosition();
        Log.e("MarkerClick", "Position: " + position);

        // Uncomment the following lines to save the position in SharedPreferences (optional)
        // SharedPreferences sharedPreferences = getSharedPreferences("LocationPrefs", MODE_PRIVATE);
        // SharedPreferences.Editor editor = sharedPreferences.edit();
        // editor.putFloat("marker_lat", (float) position.latitude);
        // editor.putFloat("marker_lng", (float) position.longitude);
        // editor.apply();

        // Log the values being sent
        Log.e("MarkerClick", "Sending lat: " + position.latitude + ", lng: " + position.longitude);

        // Navigate to the slideshow fragment with the coordinates
        Intent intent = new Intent();
        intent.setData(Uri.parse("marker_lat=" + position.latitude + "&marker_lng=" + position.longitude));
        setResult(1, intent);
        finish();
        return true;
    }
}
