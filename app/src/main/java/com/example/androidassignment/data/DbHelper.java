package com.example.androidassignment.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.androidassignment.entity.MarkerItem;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {


    public static final String MARKER_ID = "markerId";
    public static final String MAP_TABLE = "MAP_TABLE";
    public static final String MARKER_TITLE = "markerTitle";
    public static final String MARKER_IMAGE = "markerImage";
    public static final String MARKER_LAT = "markerLat";
    public static final String MARKER_LNG = "markerLng";
    public static final String MARKER_DESCRIPTION = "markerDescription";


    public DbHelper(@Nullable Context context) {
        super(context, "hiking_app.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_QUERY = "CREATE TABLE " + MAP_TABLE + " (" + MARKER_ID + " integer primary key AUTOINCREMENT, " + MARKER_TITLE + " text, " + MARKER_IMAGE + " text, "+MARKER_LAT+" numeric, "+MARKER_LNG+" numeric, "+MARKER_DESCRIPTION+" text)";
        db.execSQL(CREATE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public boolean delete(String title){
        SQLiteDatabase db = this.getWritableDatabase();
        String delete = "DELETE FROM "+MAP_TABLE+" WHERE "+MARKER_TITLE+ " = '"+title+"'";
        Cursor cursor = db.rawQuery(delete,null);
        if (cursor.moveToFirst()){
            db.close();
            cursor.close();
            return false;
        }else{
            db.close();
            cursor.close();
            return true;

        }
    }

    public MarkerItem create(MarkerItem markerItem){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MARKER_TITLE,markerItem.getMarkerTitle());
        contentValues.put(MARKER_IMAGE,markerItem.getMarkerImage());
        contentValues.put(MARKER_LAT,markerItem.getLat());
        contentValues.put(MARKER_LNG,markerItem.getLng());
        contentValues.put(MARKER_DESCRIPTION,markerItem.getDescription());

        long insertStatus = db.insert(MAP_TABLE,null,contentValues);

        if (insertStatus == -1){
            db.close();
            return null;
        }
        else{
            db.close();
            return markerItem;
        }
    }

    public MarkerItem findByTitle(String title){
        SQLiteDatabase db = getReadableDatabase();
        String findByTitleQuery = ("SELECT * FROM "+MAP_TABLE+" WHERE " +MARKER_TITLE+ " = '") +title+"'";
        Cursor cursor = db.rawQuery(findByTitleQuery,null);
        MarkerItem tempMarkerItem = null;
        if (cursor.moveToFirst()){
            int markerId = cursor.getInt(0);
            String markerTitle = cursor.getString(1);
            String markerImage = cursor.getString(2);
            double lat = cursor.getDouble(3);
            double lng = cursor.getDouble(4);
            String description = cursor.getString(5);
            tempMarkerItem = new MarkerItem(markerId,markerTitle,description,lat,lng,markerImage);
        }
        return tempMarkerItem;
    }

    public List<MarkerItem> findAll(){
        List<MarkerItem> markerItemList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String getAll = "SELECT * FROM "+MAP_TABLE;
        Cursor cursor = db.rawQuery(getAll,null);
        if (cursor.moveToFirst()){
            do {
                int markerId = cursor.getInt(0);
                String markerTitle = cursor.getString(1);
                String markerImage = cursor.getString(2);
                double lat = cursor.getDouble(3);
                double lng = cursor.getDouble(4);
                String description = cursor.getString(5);
                MarkerItem markerItem = new MarkerItem(markerId,markerTitle,description,lat,lng,markerImage);
                markerItemList.add(markerItem);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return markerItemList;
    }
}
