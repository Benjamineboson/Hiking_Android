package com.example.androidassignment;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Intent;
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
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private DbHelper dbHelper;
    private Button switchTerrainBtn;
    private GoogleMap mMap;
    private ArrayAdapter arrayAdapter;
    private ListView markerListView;
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
                        markerItemList.add(dbHelper.create(new MarkerItem(mo.getTitle(),null,latLng.latitude,latLng.longitude)));
                        arrayAdapter.notifyDataSetChanged();
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
        arrayAdapter = new ArrayAdapter<MarkerItem>(MapsActivity.this,android.R.layout.simple_list_item_1,markerItemList);
        markerListView.setAdapter(arrayAdapter);
    }
}

