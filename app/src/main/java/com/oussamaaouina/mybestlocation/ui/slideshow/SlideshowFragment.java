package com.oussamaaouina.mybestlocation.ui.slideshow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.oussamaaouina.mybestlocation.Config;
import com.oussamaaouina.mybestlocation.JSONParser;
import com.oussamaaouina.mybestlocation.R;
import com.oussamaaouina.mybestlocation.databinding.FragmentSlideshowBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SlideshowFragment extends Fragment implements LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FragmentSlideshowBinding binding;
    private LocationManager locationManager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button add = binding.addBtn;
        Button map = binding.mapBtn;
        Button back = binding.backBtn;

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // Show location if permission is granted
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            showLocation();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Handle "Back" button click
        back.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_home);
        });

        // Handle "Save Position" button click
        add.setOnClickListener(v -> {
            if (isAdded()) { // Ensure fragment is still added to the activity
                HashMap<String, String> params = new HashMap<>();
                params.put("longitude", binding.textLongitude.getText().toString());
                params.put("latitude", binding.textLatitude.getText().toString());
                params.put("numero", binding.textNumero.getText().toString());
                params.put("pseudo", binding.textPseudo.getText().toString());

                // Execute the Upload task
                new Upload(params).execute();

                // Clear input fields
                binding.textLongitude.setText("");
                binding.textLatitude.setText("");
                binding.textNumero.setText("");
                binding.textPseudo.setText("");

                // Navigate to HomeFragment
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.nav_home);
            }
        });

        // Handle "Back to Map" button click
        map.setOnClickListener(v -> {
            Intent maps = new Intent(getActivity(), MapsActivity.class);
            maps.putExtra("longitude", binding.textLongitude.getText().toString());
            maps.putExtra("latitude", binding.textLatitude.getText().toString());
            startActivityForResult(maps, 1);
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1 && data != null) {
            String dataString = data.getDataString();
            String[] parts = dataString.split("&");
            String markerLat = parts[0].split("=")[1];
            String markerLng = parts[1].split("=")[1];
            binding.textLatitude.setText(markerLat);
            binding.textLongitude.setText(markerLng);
        }
    }

    @SuppressLint("MissingPermission")
    private void showLocation() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, this);
        } else {
            Toast.makeText(getContext(), "Please turn on your GPS location", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (binding != null) {
            binding.textLatitude.setText(String.valueOf(location.getLatitude()));
            binding.textLongitude.setText(String.valueOf(location.getLongitude()));
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        // Handle provider enabled event
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        // Handle provider disabled event
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationManager != null) {
            locationManager.removeUpdates(this); // Unregister location listener
        }
        binding = null; // Avoid memory leak
    }

    // AsyncTask for uploading data
    private static class Upload extends AsyncTask<Void, Void, Void> {

        private final HashMap<String, String> params;

        public Upload(HashMap<String, String> params) {
            this.params = params;
        }

        @Override
        protected void onPreExecute() {
            // UI Thread
            // Show a loading dialog or something similar if needed
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Simulate a delay for the upload task
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeHttpRequest(Config.url_add, "POST", params);

            try {
                int success = response.getInt("success");
                if (success == 1) {
                    Log.e("Upload", "Data uploaded successfully");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // UI Thread - After background task is complete
            // Dismiss loading dialog and notify user
        }
    }
}
