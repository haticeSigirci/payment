package com.example.haticesigirci.payment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    public static final String TAG = "locationCheck";

    private static final int[] COLORS = new int[]{R.color.primary_dark, R.color.primary, R.color.primary_light, R.color.accent, R.color.primary_dark_material_light};

    //  double latitude, longitude;
    ArrayList<Route> allRoutes;
    //Google and Location Settings
    LocationManager locationManager;
    Location location;
    LatLng start, end;
    LatLng markerLatlng;
    //variables
    double payment;
    private List<Polyline> polylines;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleMap();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showSettingsAlert();
        }

        buildGoogleApiClient();


    }

    public void buildGoogleMap() {

        SupportMapFragment fragment = SupportMapFragment.newInstance();
        fragment.getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);

    }

    protected synchronized void buildGoogleApiClient() {

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    protected void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }


    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext());
        alertDialog.setTitle("GPS is settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 1);

                dialog.dismiss();
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();

    }

    public void drawPoliyline(ArrayList<Route> route, int whichRoute) {
        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        //add route(s) to the map.

        //  for (int i = 0; i < route.size(); i++) {

        //In case of more than 5 alternative routes
        int colorIndex = whichRoute % COLORS.length;

        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(getResources().getColor(COLORS[colorIndex]));
        polyOptions.width(10 + whichRoute * 5);
        polyOptions.addAll(route.get(whichRoute).getPoints());

        Polyline polyline = map.addPolyline(polyOptions);
        polyline.isClickable();
        polylines.add(polyline);

        Toast.makeText(getApplicationContext(), "Route " + (whichRoute + 1) + ": distance - " + route.get(whichRoute).getDistanceValue() + ": duration - " + route.get(whichRoute).getDurationValue(), Toast.LENGTH_LONG).show();

        double result = calculatePaymentInTL(route.get(whichRoute).getDistanceValue());

        Toast.makeText(getApplicationContext(), "TL = " + String.valueOf(result), Toast.LENGTH_LONG).show();

        Log.d("LocationDistance", String.valueOf(route.get(whichRoute).getDistanceValue()));
        Log.d("LocationDuration", String.valueOf(route.get(whichRoute).getDurationValue()));


        MarkerOptions options = new MarkerOptions();
        options.position(start);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue));
        map.addMarker(options);

        // End marker
        options = new MarkerOptions();
        options.position(end);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
        map.addMarker(options);

    }

    public void calculateRoute() {

        Log.d("startLatitude", String.valueOf(start));
        Log.d("endLatitude", String.valueOf(end));


        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(start, end)
                .alternativeRoutes(true)
                .build();
        routing.execute();

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        }

        createLocationRequest();

        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null) {
            //        latitude = Double.valueOf(location.getLatitude());
            //        longitude = Double.valueOf(location.getLongitude());

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            start = latLng;
            //    map.addMarker(new MarkerOptions().position(latLng).title("hiyk"));


          /*  CameraUpdate myLocation = CameraUpdateFactory.newLatLng(latLng);

            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

            map.moveCamera(myLocation);
            map.animateCamera(zoom);*/


            //      Log.d("location", String.valueOf(latitude));
            //      Log.d("location", String.valueOf(longitude));


        } else {
            Log.d(TAG, "(Couldn't get the location");
        }
    }

    private double calculatePaymentInTL(int distanceValue) {

        int kilometers = distanceValue / 1000;
        int meters = distanceValue - kilometers * 1000;

        payment = 3.45 + (distanceValue * 2.10) + (meters * 0.0021);

        return payment;

    }


    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        displayLocation();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onRoutingFailure(RouteException e) {

        Log.d("locationTag", "onRoutingFailure");

        if (e != null) {
            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

        Log.d("locationTag", "onRoutingStart");

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int i) {

        Log.d("locationTag", "onRoutingSuccess");

        allRoutes = new ArrayList<Route>(route);

    }

    @Override
    public void onRoutingCancelled() {

        Log.d("locationTag", "onRoutingCancelled");

        Log.i(TAG, "Routing was cancelled.");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).title("Varış Noktası");
                markerOptions.draggable(true);

                markerLatlng = latLng;
                map.addMarker(markerOptions);

                end = markerLatlng;


                calculateRoute();
            }
        });

    }
}
