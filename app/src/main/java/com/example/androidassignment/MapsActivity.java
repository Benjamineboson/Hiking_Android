package com.example.androidassignment;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidassignment.data.DbHelper;
import com.example.androidassignment.entity.MarkerItem;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private DbHelper dbHelper;
    private Button switchTerrainBtn;
    private GoogleMap mMap;
    private ArrayAdapter arrayAdapter;
    private ListView markerListView;
    private TextView errorTextView;
    private static final int ACCESS_FINE_LOCATION_CODE = 1;
    public static List<LatLng> markerLatLngList = new ArrayList<>();
    public static List<String> markerTitleList = new ArrayList<>();
    public static List<MarkerItem> markerItemList;
    private static int mapTerrainCounter = 2;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setBarStyle();
        markerListView = findViewById(R.id.markerListView);
        dbHelper = new DbHelper(MapsActivity.this);
        switchTerrainBtn = findViewById(R.id.switchTerrainBtn);
        switchTerrainBtn.setBackgroundColor(getResources().getColor(android.R.color.white));
        markerItemList = dbHelper.findAll();
        updateList();
        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey("points")){
                markerLatLngList = savedInstanceState.getParcelableArrayList("markerLatLng");
                markerTitleList = savedInstanceState.getStringArrayList("markerTitle");
            }
        }
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(2);
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(MapsActivity.this, "Already granted permission", Toast.LENGTH_SHORT).show();
            mMap.setMyLocationEnabled(true);
        }else{
            requestPermission();
        }
        if(markerTitleList.size() > 0 && markerLatLngList.size() > 0){
            for(int i=0;i<markerLatLngList.size() & i< markerTitleList.size();i++){
                drawMarkers(markerLatLngList.get(i),markerTitleList.get(i));
            }
        }
        if (markerItemList.size() > 0){
            for (int i = 0; i < markerItemList.size(); i++) {
                drawMarkers(new LatLng(markerItemList.get(i).getLat(),markerItemList.get(i).getLng()),markerItemList.get(i).getMarkerTitle());
            }
        }
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                final String[] title = {""};
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapsActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.new_marker_dialog,null);
                alertDialogBuilder.setView(dialogView);
                alertDialogBuilder.setCancelable(false);
                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                Button markerTitleBtn = dialogView.findViewById(R.id.markerTitleBtn);
                final EditText setMarkerTitleInput = dialogView.findViewById(R.id.setMarkerTitleInput);
                errorTextView = dialogView.findViewById(R.id.errorTextView);
                markerTitleBtn.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View v) {
                            title[0] = setMarkerTitleInput.getText().toString();
                            MarkerOptions mo = new MarkerOptions().position(latLng).title(title[0].trim());
                            if (dbHelper.findByTitle(title[0]) != null){
                                errorTextView.setText("A marker with that title already exists");
                            }else if(title[0].trim().length() < 1){
                                errorTextView.setText("Please enter a title for your marker");
                            }else{
                                mMap.addMarker(mo);
                                markerItemList.add(dbHelper.create(new MarkerItem(mo.getTitle(),null,latLng.latitude,latLng.longitude)));
                                arrayAdapter.notifyDataSetChanged();
                                markerLatLngList.add(latLng);
                                markerTitleList.add(mo.getTitle());
                                alertDialog.hide();
                            }
                    }
                });
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final Intent act2 = new Intent(MapsActivity.this, MarkerInfoActivity.class);
                act2.putExtra("TITLE",marker.getTitle());
                act2.putExtra("LOCATION",marker.getPosition());
                startActivity(act2);
                return false;
            }
        });
        switchTerrainBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mapTerrainCounter > 4){
                            mapTerrainCounter = 1;
                        }
                        mMap.setMapType(++mapTerrainCounter);
                    }
                });
        markerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(markerItemList.get(position).getLat(),markerItemList.get(position).getLng())));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12.5f));
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("markerLatLng", new ArrayList<Parcelable>(markerLatLngList));
        outState.putStringArrayList("markerTitle", new ArrayList<>(markerTitleList));
    }

    public void drawMarkers(LatLng latLng, String title){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng).title(title);
        mMap.addMarker(markerOptions).setTitle(title);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void setBarStyle(){
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(MapsActivity.this,android.R.color.white));
    }

    private void updateList(){
        arrayAdapter = new ArrayAdapter<MarkerItem>(MapsActivity.this,R.layout.my_list_view,markerItemList);
        markerListView.setAdapter(arrayAdapter);
    }


    private void requestPermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)){
            new androidx.appcompat.app.AlertDialog.Builder(MapsActivity.this).setTitle("Permission needed").setMessage("Needed to show your current location")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},ACCESS_FINE_LOCATION_CODE);
                            mMap.setMyLocationEnabled(true);
                            dialog.dismiss();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
                    .create().show();
        }else{
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},ACCESS_FINE_LOCATION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ACCESS_FINE_LOCATION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mMap.setMyLocationEnabled(true);
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

