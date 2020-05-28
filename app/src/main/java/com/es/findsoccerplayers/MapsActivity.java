package com.es.findsoccerplayers;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String placename;
    private double mlatitude;
    private double mlongitude;
    private LatLng initPosition;
    private LatLng myPosition;


    public final String LONGITUDE = "longitude";
    public final String LATITUDE = "latitude";
    public final String PLACE_NAME = "place name";
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    LocationManager mLocationManager;
    LocationCallback mLocationCallback;
    Location mLastLocation;



    private FloatingActionButton confirm_fab;
    private FloatingActionButton position_fab;


    //Location objects and parameters
    FusedLocationProviderClient mFusedLocationClient;
    private boolean gpsON = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        confirm_fab = findViewById(R.id.confirm_fab);
        position_fab = findViewById(R.id.position_fab);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //create a callback when receive a location result
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                myPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 15));
            }
        };




        //check if the GPS is disabled
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //if the GPS is disabled show a Toast
            Toast.makeText(MapsActivity.this, R.string.gps_disabled_toast, Toast.LENGTH_SHORT).show();
            gpsON = false;
            //TODO: Get the position from the user shared Preferencies
            // initposition = (user shared preferencies)
        }else {
                //Use the current position of the user
                gpsON = true;
                position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.blue)));
                startTrackingLocation();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        confirm_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(LATITUDE, mlatitude);
                resultIntent.putExtra(LONGITUDE, mlongitude);
                resultIntent.putExtra(PLACE_NAME, placename);
                setResult(RESULT_OK, resultIntent);
                stopTrackingLocation();
                finish();
            }
        });

        position_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    //if the GPS is disabled show a Toast or an AlertDialog
                    Toast.makeText(MapsActivity.this, R.string.gps_disabled_toast, Toast.LENGTH_SHORT).show();
                    position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
                    gpsON = false;
                }else {
                    // Go to the new view in the map and change the color
                    gpsON = true;
                    startTrackingLocation();
                    position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.blue)));
                }

            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION_PERMISSION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Draw my position in the map
                    enableMyLocation();
                    break;
                }
        }
    }

    //Insert the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_options, menu);
        return true;
    }

    //Using Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Change the map type based on the user's selection.
        switch (item.getItemId()) {
            case R.id.normal_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();

        //If the user have a last know location the map will start from there
        // if there is not a last know location the entire world map will appear and the user will find his location
        // by pressing the button in the top right corner of the map.

        setMapLongClick(mMap); // Add a marker in a long position click
        setPoiClick(mMap);  //add a marker in a POI
        setInfoWindowclick(mMap); // choose the selectet position if the user click in the info window
        onMapMoving(mMap); //if the camera moves because the user move it, stop tracking location. He's looking for a place.
    }



    // onMapMoving stop the tracking location if the user star moving the camera.
    private void onMapMoving(GoogleMap map){
        map.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE){
                    stopTrackingLocation();
                }
            }
        });
    }



    //If the user make a long click on the map, add in that position a marker and show an info window
    private void setMapLongClick(final GoogleMap map){
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.clear();
                String snippet = String.format(Locale.getDefault(), "Lat: %1.5f, Long: %2.5f",
                        latLng.latitude,
                        latLng.longitude);
                try {
                    Geocoder geo = new Geocoder(MapsActivity.this.getApplicationContext(), Locale.getDefault());
                    List<Address> addresses = geo.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addresses.isEmpty()) {
                        placename = getString(R.string.default_position_name);
                    } else {
                        if (addresses.size() > 0) {
                            placename = addresses.get(0).getLocality();  // if the place is not a POI the use only the City name
                            mlatitude = latLng.latitude;  //get the selected place latitude
                            mlongitude = latLng.longitude; // //get the selected place longitude
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MapsActivity.this, R.string.gecodo_failed, Toast.LENGTH_SHORT).show();
                    placename = getString(R.string.default_position_name);
                }
                Marker myMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(placename).snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                myMarker.showInfoWindow();
            }
        });

    }


    /*POI is the "Point Of Interes". Many structure alredy exist in
    * Google database with their name. We can set the position name name from the POI name */
    private void setPoiClick(final GoogleMap map){
        map.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest pointOfInterest) {
                mMap.clear();
                Marker poiMarker = mMap.addMarker(new MarkerOptions().position(pointOfInterest.latLng).title(pointOfInterest.name)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                poiMarker.showInfoWindow();
                placename = pointOfInterest.name; //get the poi name
                mlatitude = pointOfInterest.latLng.latitude;  // get the poi latitude
                mlongitude = pointOfInterest.latLng.longitude; // get the poi longitude
            }
        });
    }

    private void setInfoWindowclick(final GoogleMap map){
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(LATITUDE, mlatitude);
                resultIntent.putExtra(LONGITUDE, mlongitude);
                resultIntent.putExtra(PLACE_NAME, placename);
                setResult(RESULT_OK, resultIntent);
                stopTrackingLocation();
                finish();
            }
        });
    }


    //enable showing my location. Check self permission first
    private void enableMyLocation(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true); // show a small blue circle for my position
            mMap.getUiSettings().setMyLocationButtonEnabled(false);// I create my own beatiful position floating anction button
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

    }


    //Location Request to build the Location Callback.
    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    //start tracking location
    private void startTrackingLocation(){
        mFusedLocationClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, null);
    }

    //stop tracking location
    private void stopTrackingLocation(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

}
