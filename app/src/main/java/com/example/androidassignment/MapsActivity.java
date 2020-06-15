package com.example.androidassignment;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.androidassignment.data.DbHelper;
import com.example.androidassignment.entity.MarkerItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private DbHelper dbHelper;
    private Button switchTerrainBtn;
    private GoogleMap mMap;
    public static List<LatLng> markerLatLngList = new ArrayList<>();
    public static List<String> markerTitleList = new ArrayList<>();
    public static List<MarkerItem> markerItemList;
    private static int counter = 1;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setBarStyle();
        dbHelper = new DbHelper(MapsActivity.this);
        switchTerrainBtn = findViewById(R.id.switchTerrainBtn);
        switchTerrainBtn.setBackgroundColor(getResources().getColor(android.R.color.white));
        markerItemList = dbHelper.findAll();
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

                markerTitleBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        title[0] = setMarkerTitleInput.getText().toString();
                        MarkerOptions mo = new MarkerOptions().position(latLng).title(title[0]);
                        mMap.addMarker(mo);
                        dbHelper.create(new MarkerItem(mo.getTitle(),latLng.latitude,latLng.longitude));
                        markerLatLngList.add(latLng);
                        markerTitleList.add(mo.getTitle());
                        alertDialog.hide();
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
                        if (counter > 4){
                            counter = 1;
                        }
                        mMap.setMapType(++counter);
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
}

