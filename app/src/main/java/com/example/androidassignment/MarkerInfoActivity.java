package com.example.androidassignment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidassignment.data.DbHelper;
import com.example.androidassignment.entity.MarkerItem;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MarkerInfoActivity extends AppCompatActivity {

    public static final int REQUEST_UPLOAD_IMAGE = 1;
    public static final int STORAGE_PERMISSION_CODE = 1;
    public static final int THUMBNAIL_HEIGHT = 1000;
    public static final int THUMBNAIL_WIDTH = 1200;
    TextView act2Tv;
    private DbHelper dbHelper;
    private MarkerItem markerItem;
    private ImageView markerImageView;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        requestQueue = Volley.newRequestQueue(MarkerInfoActivity.this);
        dbHelper = new DbHelper(MarkerInfoActivity.this);
        markerItem = dbHelper.findByTitle(getIntent().getStringExtra("TITLE"));
        httpGetRequest();
        act2Tv = findViewById(R.id.markerTextView);
        markerImageView = findViewById(R.id.markerImageView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setBarStyle();
        }
        if (markerItem.getMarkerImage() != null){
            try{
                Bitmap bm = BitmapFactory.decodeFile(markerItem.getMarkerImage());
                bm = Bitmap.createScaledBitmap(bm,THUMBNAIL_WIDTH,THUMBNAIL_HEIGHT,false);
                markerImageView.setImageBitmap(bm);
            }catch (RuntimeException e){
                Toast.makeText(this, "Image too large", Toast.LENGTH_SHORT).show();
            }

        }

        if (markerItem.getDescription() != null){
            act2Tv.setText(markerItem.getDescription());
        }

        if (ContextCompat.checkSelfPermission(MarkerInfoActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Already granted permission", Toast.LENGTH_SHORT).show();
        }else{
            requestStoragePermission();
        }


    }

    private void requestStoragePermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(MarkerInfoActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(MarkerInfoActivity.this).setTitle("Permission needed").setMessage("Needed to upload photo dumdum")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MarkerInfoActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .create().show();
        }else{
           ActivityCompat.requestPermissions(MarkerInfoActivity.this,
                   new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.deleteItem:
                deleteMarkerItem();
                break;
            case R.id.uploadImageItem:
                uploadImage();
                break;
            case R.id.descriptionItem:
                addDescription();
                break;
        }
        return true;
    }

    private void addDescription() {
        final String[] title = {""};
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(MarkerInfoActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_description_dialog,null);
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setCancelable(false);
        final android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        Button markerTitleBtn = dialogView.findViewById(R.id.descpritionBtn);
        final EditText setMarkerTitleInput = dialogView.findViewById(R.id.descriptionInput);
        markerTitleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title[0] = setMarkerTitleInput.getText().toString();
                act2Tv.setText(title[0]);
                markerItem.setDescription(title[0]);
                dbHelper.delete(markerItem.getMarkerTitle());
                dbHelper.create(markerItem);
                alertDialog.hide();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void setBarStyle(){
        setTitle(markerItem.getMarkerTitle());
        ActionBar actionBar;
        actionBar = getSupportActionBar();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(MarkerInfoActivity.this,android.R.color.white));
        ColorDrawable colorDrawable = new ColorDrawable(Color.GRAY);
        actionBar.setBackgroundDrawable(colorDrawable);
    }

    private void deleteMarkerItem(){
        MapsActivity.markerLatLngList.remove(getIntent().getStringExtra("LOCATION"));
        MapsActivity.markerTitleList.remove(getIntent().getStringExtra("TITLE"));
        dbHelper.delete(markerItem.getMarkerTitle());
        startActivity(new Intent(MarkerInfoActivity.this,MapsActivity.class));
    }

    private void uploadImage(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_UPLOAD_IMAGE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            String picturePath = getPath(MarkerInfoActivity.this, selectedImageUri);
            Bitmap bm = BitmapFactory.decodeFile(picturePath);
            bm = Bitmap.createScaledBitmap(bm,THUMBNAIL_WIDTH,THUMBNAIL_HEIGHT,false);
            markerItem.setMarkerImage(picturePath);
            dbHelper.delete(markerItem.getMarkerTitle());
            dbHelper.create(markerItem);
            markerImageView.setImageBitmap(bm);
        }
    }


    public static String getPath(Context context, Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }
    
    public void httpGetRequest(){
        String url = "https://api.worldweatheronline.com/premium/v1/weather.ashx?key=82f5c6c5357b4b4091271741201706&q="+markerItem.getLat()+","+markerItem.getLng()+"&format=json&num_of_days=5";
        JsonObjectRequest myGetReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                act2Tv.setText(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(myGetReq);
    }


}
