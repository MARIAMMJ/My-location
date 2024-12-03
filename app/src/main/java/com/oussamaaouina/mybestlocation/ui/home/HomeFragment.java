package com.oussamaaouina.mybestlocation.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.oussamaaouina.mybestlocation.Config;
import com.oussamaaouina.mybestlocation.JSONParser;
import com.oussamaaouina.mybestlocation.Position;
import com.oussamaaouina.mybestlocation.PositionAdapter;
import com.oussamaaouina.mybestlocation.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    // Data list for RecyclerView
    private final ArrayList<Position> data = new ArrayList<>();
    private PositionAdapter adapter; // RecyclerView adapter
    private FragmentHomeBinding binding; // View binding
    private AlertDialog alert; // Alert dialog for loading indication

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout using view binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup RecyclerView
        setupRecyclerView();

        // Request location permissions
        ActivityCompat.requestPermissions(this.getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        // Button click listener to start download
        binding.downloadBtn.setOnClickListener(v -> new Download().execute());

        return root;
    }

    /**
     * Sets up the RecyclerView and its adapter.
     */
    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Vertical layout
        adapter = new PositionAdapter(getContext(), data); // Initialize the adapter
        recyclerView.setAdapter(adapter); // Attach the adapter
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * AsyncTask for downloading and parsing data in the background.
     */
    class Download extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            // Show a loading dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Download");
            builder.setMessage("Downloading...");
            alert = builder.create();
            alert.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Fetch and parse JSON data
            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeRequest(Config.url_getAll);

            try {
                if (response != null) {
                    int success = response.getInt("success");
                    if (success == 1) {
                        data.clear(); // Clear existing data
                        JSONArray positions = response.getJSONArray("positions");
                        for (int i = 0; i < positions.length(); i++) {
                            JSONObject obj = positions.getJSONObject(i);
                            int id = obj.getInt("id");
                            String pseudo = obj.getString("pseudo");
                            String longitude = obj.getString("longitude");
                            String latitude = obj.getString("latitude");
                            String numero = obj.getString("numero");

                            // Add new Position object to the data list
                            data.add(new Position(id, pseudo, longitude, latitude, numero));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Dismiss the loading dialog
            if (alert != null && alert.isShowing()) {
                alert.dismiss();
            }

            // Notify adapter of data changes
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Handles location permission results.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this.getContext(), "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this.getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}