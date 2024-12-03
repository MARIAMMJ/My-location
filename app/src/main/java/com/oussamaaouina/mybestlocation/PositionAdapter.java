package com.oussamaaouina.mybestlocation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oussamaaouina.mybestlocation.ui.slideshow.MapsActivity;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.PositionViewHolder> {

    private final Context context;
    private final ArrayList<Position> positions;

    public PositionAdapter(Context context, ArrayList<Position> positions) {
        this.context = context;
        this.positions = positions;
    }

    @NonNull
    @Override
    public PositionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for the RecyclerView
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_position, parent, false);
        return new PositionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PositionViewHolder holder, int position) {
        Position currentPosition = positions.get(position);

        // Bind data to the views
        holder.textViewPseudo.setText("Pseudo: " + currentPosition.getPseudo());
        holder.textViewLongitude.setText("Longitude: " + currentPosition.getLongitude());
        holder.textViewLatitude.setText("Latitude: " + currentPosition.getLatitude());
        holder.textViewNumero.setText("Numero: " + currentPosition.getNumero());

        // Handle Delete button click
        holder.btnDelete.setOnClickListener(v -> {
            // Call the API to delete the position
            String id = String.valueOf(currentPosition.getId());
            deletePositionFromServer(id, position);
        });

        // Handle "Open in Maps" button
        holder.btnOpenMap.setOnClickListener(v -> {
            Intent intent = new Intent(context, MapsActivity.class);
            intent.putExtra("latitude", currentPosition.getLatitude());
            intent.putExtra("longitude", currentPosition.getLongitude());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return positions.size();
    }

    static class PositionViewHolder extends RecyclerView.ViewHolder {

        TextView textViewPseudo, textViewLongitude, textViewLatitude, textViewNumero;
        Button btnDelete, btnOpenMap;

        public PositionViewHolder(@NonNull View itemView) {
            super(itemView);

            // Bind the views from the item layout
            textViewPseudo = itemView.findViewById(R.id.textViewPseudo);
            textViewLongitude = itemView.findViewById(R.id.textViewLongitude);
            textViewLatitude = itemView.findViewById(R.id.textViewLatitude);
            textViewNumero = itemView.findViewById(R.id.textViewNumero);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnOpenMap = itemView.findViewById(R.id.btnOpenMap);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void deletePositionFromServer(String id, int position) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    URL url = new URL(Config.url_delete);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    // Send the ID in the request body
                    String data = "id=" + id;
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(data.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    // Check the response code
                    int responseCode = connection.getResponseCode();
                    return responseCode == 200;  // Return true if the deletion is successful
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                if (success) {
                    // Remove the item from the positions list and notify the adapter
                    positions.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Position deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to delete position", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();  // Execute the AsyncTask
    }
}