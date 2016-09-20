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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import lt.lemonlabs.android.expandablebuttonmenu.ExpandableButtonMenu;
import lt.lemonlabs.android.expandablebuttonmenu.ExpandableMenuOverlay;

/**
 * Created by haticesigirci on 27/08/16.
 */
public class GoogleMapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    public static final String TAG = "locationCheck";
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final int[] COLORS = new int[]{R.color.primary_dark, R.color.primary, R.color.primary_light, R.color.accent, R.color.primary_dark_material_light};
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters
    Location location;// location
    // flag for GPS status
    boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Bundle bundle;
    LatLng markerLatlng;
    LocationManager locationManager;
    LatLng start, end;
    ExpandableMenuOverlay menuOverlay;
    double payment;

    //  double latitude, longitude;
    ArrayList<Route> allRoutes;
    private boolean mRequestingLocationUpdates;  //use as a flag need research
    // LocationManager locationManager;
    // LocationListener locationListener;
    private String provider;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private GoogleMap map;
    private List<Polyline> polylines;
    private Button cleanButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, null, false);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        Log.d(TAG, "hatice123");

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

        cleanButton = (Button) view.findViewById(R.id.btn_clean_map);

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showSettingsAlert();
        }

        menuOverlay = (ExpandableMenuOverlay) view.findViewById(R.id.button_menu);
        menuOverlay.setOnMenuButtonClickListener(new ExpandableButtonMenu.OnMenuButtonClick() {
            @Override
            public void onClick(ExpandableButtonMenu.MenuButton action) {
                switch (action) {
                    case MID:
                        Toast.makeText(getContext(), "Mid pressed", Toast.LENGTH_SHORT).show();
                        drawPoliyline(allRoutes, 1);
                        //  menuOverlay.getButtonMenu().toggle();
                        break;
                    case LEFT:
                        Toast.makeText(getContext(), "Left pressed", Toast.LENGTH_SHORT).show();
                        drawPoliyline(allRoutes, 0);
                        break;
                    case RIGHT:
                        Toast.makeText(getContext(), "Right pressed", Toast.LENGTH_SHORT).show();
                        drawPoliyline(allRoutes, 2);
                        break;
                }
            }
        });


        cleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                map.clear();

            }
        });

        buildGoogleApiClient();
        MapsInitializer.initialize(getContext());
        mGoogleApiClient.connect();

        polylines = new ArrayList<>();

        displayLocation();

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showSettingsAlert();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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


    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        displayLocation();
    }


    private void setUpMap() {
        map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    public void zoomMyLocation(double latitudeVal, double longitudeVal) {

        LatLng latLng = new LatLng(latitudeVal, longitudeVal);

        CameraUpdate myLocation = CameraUpdateFactory.newLatLng(latLng);

        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        map.moveCamera(myLocation);
        map.animateCamera(zoom);


    }


    protected synchronized void buildGoogleApiClient() {

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = ");
        buildGoogleApiClient();

    }

    @Override
    public void onConnected(Bundle bundle) {

        displayLocation();

    }

    protected void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }


    @TargetApi(Build.VERSION_CODES.M)
    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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
            map.addMarker(new MarkerOptions().position(latLng).title("hiyk"));


            CameraUpdate myLocation = CameraUpdateFactory.newLatLng(latLng);

            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

            map.moveCamera(myLocation);
            map.animateCamera(zoom);


            //      Log.d("location", String.valueOf(latitude));
            //      Log.d("location", String.valueOf(longitude));


        } else {
            Log.d(TAG, "(Couldn't get the location");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }


    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
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

    @Override
    public void onRoutingFailure(RouteException e) {

        Log.d("locationTag", "onRoutingFailure");

        if (e != null) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

        Log.d("locationTag", "onRoutingStart");


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

        Toast.makeText(getContext(), "Route " + (whichRoute + 1) + ": distance - " + route.get(whichRoute).getDistanceValue() + ": duration - " + route.get(whichRoute).getDurationValue(), Toast.LENGTH_LONG).show();

        double result = calculatePaymentInTL(route.get(whichRoute).getDistanceValue());

        Toast.makeText(getContext(), "TL = " + String.valueOf(result), Toast.LENGTH_LONG).show();

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

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int whichRoute) {


        Log.d("locationTag", "onRoutingSuccess");

        allRoutes = new ArrayList<Route>(route);


    }


    private double calculatePaymentInTL(int distanceValue) {

        int kilometers = distanceValue / 1000;
        int meters = distanceValue - kilometers * 1000;

        payment = 3.45 + (distanceValue * 2.10) + (meters * 0.0021);

        return payment;

    }

    @Override
    public void onRoutingCancelled() {

        Log.d("locationTag", "onRoutingCancelled");


        Log.i(TAG, "Routing was cancelled.");
    }
}
