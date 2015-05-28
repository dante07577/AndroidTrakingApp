package com.example.dimas.trakingapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.views.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Home extends ActionBarActivity {

    Marker marker;
    MapView mapView;
    LatLng  lastLatLng;
    boolean rec=false;
    File sdPath;
    File sdFile;
    PathOverlay line = new PathOverlay(Color.RED, 3);
    Context context = this;

    private LocationManager locationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        this.findViewById(R.id.mapview);
        mapView = (MapView)this.findViewById(R.id.mapview);
        sdPath = Environment.getExternalStorageDirectory();
        sdPath = new File(sdPath.getAbsolutePath() + "/" + "TrackingApp");
        Intent genIntent = getIntent();
        Bundle b = genIntent.getExtras();
        int fileNumber = -1;
        if(b!=null)
        {
            fileNumber =(int)(long) b.get("fileNumber");
        }
        if (savedInstanceState != null) {
            mapView.setCenter(new LatLng(savedInstanceState.getDouble("maplat"), savedInstanceState.getDouble("maplon")));
            mapView.setZoom(savedInstanceState.getFloat("mapzoom"));
        }else{
            mapView.setCenter(new LatLng(49.413, 26.96));
            mapView.setZoom(15);
            lastLatLng = new LatLng(100, 100);
        }
        final Button meButton= (Button) findViewById(R.id.button2);
        final Button startButton= (Button) findViewById(R.id.button3);
        final Button routesButton= (Button) findViewById(R.id.routeButton);
        View.OnClickListener onClickRoutesButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context,Route.class);
                startActivity(intent);
                finish();
            }
        };
        View.OnClickListener onClickMeButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lastLatLng.getLatitude() !=100 && lastLatLng.getLongitude()!=100)
                {
                    mapView.setCenter(lastLatLng);
                }
            }
        };
        View.OnClickListener onClickStartButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rec = !rec;
                if(rec)
                {
                    startButton.setText("Stop recording");
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        Log.e("SD problems", "SD-card error: " + Environment.getExternalStorageState());
                    }
                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
                    Date date = new Date();
                    sdFile = new File(sdPath, dateFormat.format(date) + ".rout");
                    try {
                        sdFile.createNewFile();
                    } catch (Exception e) {
                        Log.e("File", "Create file - error");
                    }

                    if(lastLatLng.getLatitude() !=100 && lastLatLng.getLongitude()!=100)
                    {
                        line.addPoint(lastLatLng);
                    }
                }
                else
                {
                    startButton.setText("Start recording");
                }

            }
        };
        meButton.setOnClickListener(onClickMeButton);
        routesButton.setOnClickListener(onClickRoutesButton);
        startButton.setOnClickListener(onClickStartButton);
        mapView.getOverlays().add(line);
        marker = new Marker(mapView, "Your current position", null, null);
        marker.setPoint(new LatLng(-90, 0));
        mapView.addMarker(marker);
        marker.setIcon(new Icon(getResources().getDrawable(R.drawable.greenmarker)));
        if(fileNumber>=0){
            File [] files = sdPath.listFiles();
            File routeFile = files[fileNumber];
            PathOverlay openedLine = new PathOverlay(Color.BLACK, 5);
            mapView.getOverlays().add(openedLine);
            String lineStr ="";
            try {
                BufferedReader br = new BufferedReader(new FileReader(routeFile));
                boolean flag = true;
                while ((lineStr = br.readLine()) != null) {
                    String latitudeStr = lineStr.substring(0,lineStr.indexOf(","));
                    double latitude = Double.parseDouble(latitudeStr);
                    String longitudeStr = lineStr.substring(lineStr.indexOf(",")+1,lineStr.length());
                    double longitude = Double.parseDouble(longitudeStr);
                    if(flag)
                    {
                        mapView.setCenter(new LatLng(latitude,longitude));
                        flag = false;
                        Marker markerStart = new Marker(mapView, "Start point", null, null);
                        markerStart.setPoint(new LatLng(latitude, longitude));
                        mapView.addMarker(markerStart);
                    }
                    openedLine.addPoint(new LatLng((double)latitude, (double)longitude));
                }
                br.close();
            }
            catch (IOException e) {
                Log.e("SD","Read file error");
            }
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("maplat", mapView.getCenter().getLatitude());
        outState.putDouble("maplon", mapView.getCenter().getLongitude());
        outState.putFloat("mapzoom", mapView.getZoomLevel());
    }
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 20, locationListener);
    }
    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            lastLatLng = getLatLng(location);
            marker.setPoint(new LatLng(location.getLatitude(), location.getLongitude()));
            if(rec)
            {
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile,true));
                    String temp = lastLatLng.toString();
                    temp = temp.substring(0,temp.length()-4);
                    bw.append(temp + "\n");
                    bw.close();
                    line.addPoint(getLatLng(location));
                } catch (IOException e) {
                    Log.e("File", "File write error");
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e("||| lol",""+ locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER) + " ] " + locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e("||| olo",""+ locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)+ " ] " + locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    private LatLng getLatLng(Location loc){
        return new LatLng(loc.getLatitude(),loc.getLongitude());
    }




    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
//***************************************Json parse*****************************************************
/*String a = "[{\"lat\":49.31079887964633,\"lng\":27.59765625},{\"lat\":47.857402894658236,\"lng\":30.41015625}]";
                    JSONArray jsona;
                    JSONObject jsono;
                    try {
                        jsona = new JSONArray(a);
                        for (int i = 0; i < jsona.length(); i++) {
                            jsono = jsona.getJSONObject(i);
                            Log.e("LATITUDE",jsono.getString("lat"));
                        }
                    } catch (JSONException e) {
                        Log.e("MYAPP", "unexpected JSON exception", e);
                    }*/

/*********************************PopUp Window**********************************************************
 Toast.makeText(getApplicationContext(), "itemClick: position = " +
 position + ", id = " + id + ", " + parent.getAdapter().getItem(position),
 Toast.LENGTH_SHORT).show();
* */