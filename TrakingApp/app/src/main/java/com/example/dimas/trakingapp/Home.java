package com.example.dimas.trakingapp;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.views.MapView;


public class Home extends ActionBarActivity {

    Marker marker;
    private LocationManager locationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        this.findViewById(R.id.mapview);
        MapView mapView = (MapView)this.findViewById(R.id.mapview);
        mapView .setCenter(new LatLng(49.413, 26.96));
        mapView .setZoom(15);
        /*PathOverlay line = new PathOverlay(Color.RED, 3);
        line.addPoint(new LatLng(49.415775, 26.973752));
        line.addPoint(new LatLng(49.417701, 26.966543));
        line.addPoint(new LatLng(49.416976, 26.959290));
        line.addPoint(new LatLng(49.415663, 26.956329));
        line.addPoint(new LatLng(49.412453, 26.939120));
        line.addPoint(new LatLng(49.411810, 26.939291));
        line.addPoint(new LatLng(49.405751, 26.954183));
        line.addPoint(new LatLng(49.404215, 26.964011));
        mapView.getOverlays().add(line);*/
        marker = new Marker(mapView, "Your current position", null, new LatLng(49.415775,26.973752));
        mapView.addMarker(marker);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        marker.setPoint(new LatLng(49.404215, 26.964011));
        GpsLocationProvider provider = new GpsLocationProvider(this);
        UserLocationOverlay myLocationOverlay = new UserLocationOverlay(provider, mapView);
        myLocationOverlay .enableMyLocation();
        myLocationOverlay .setDrawAccuracyEnabled(true);
        mapView.getOverlays().add(myLocationOverlay);
    }
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 100, locationListener);
    }
    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            marker.setPoint(new LatLng(location.getLatitude(),location.getLongitude()));
            marker.setDescription(location.getProvider());
            Log.e("PROVIDER:" + location.getProvider(),"" + location.getLatitude());
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
