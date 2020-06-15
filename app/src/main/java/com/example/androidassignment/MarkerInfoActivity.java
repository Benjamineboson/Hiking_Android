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
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import com.example.androidassignment.data.DbHelper;
import com.example.androidassignment.entity.MarkerItem;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.Permission;

import pub.devrel.easypermissions.EasyPermissions;

public class MarkerInfoActivity extends AppCompatActivity {

    public static final int REQUEST_UPLOAD_IMAGE = 1;
    public static final int STORAGE_PERMISSION_CODE = 1;
    TextView act2Tv;
    private DbHelper dbHelper;
    private MarkerItem markerItem;
    private ImageView markerImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        dbHelper = new DbHelper(MarkerInfoActivity.this);
        markerItem = dbHelper.findByTitle(getIntent().getStringExtra("TITLE"));
        act2Tv = findViewById(R.id.markerTextView);
        act2Tv.setText(markerItem.getMarkerTitle());
        markerImageView = findViewById(R.id.markerImageView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setBarStyle();
        }
        if (markerItem.getMarkerImage() != null){
            Bitmap bitmap = BitmapFactory.decodeByteArray(markerItem.getMarkerImage(), 0, markerItem.getMarkerImage().length);
            markerImageView.setImageBitmap(bitmap);
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
        }
        return true;
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
        dbHelper.delete(act2Tv.getText().toString());
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
            markerImageView.setImageBitmap(bm);
            System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX           "+picturePath);
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


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            try {
//                final Uri imageUri = data.getData();
//                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
//                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                markerImageView.setImageBitmap(selectedImage);
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                selectedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                byte[] byteArray = stream.toByteArray();
//                markerItem.setMarkerImage(byteArray);
//                dbHelper.delete(markerItem.getMarkerTitle());
//                dbHelper.create(markerItem);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                Toast.makeText(MarkerInfoActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
//            }
//        }else {
//            Toast.makeText(MarkerInfoActivity.this, "No image selected",Toast.LENGTH_LONG).show();
//        }
//    }
}
