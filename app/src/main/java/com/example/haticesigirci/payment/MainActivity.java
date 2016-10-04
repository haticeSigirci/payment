package com.example.haticesigirci.payment;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.fivehundredpx.android.blur.BlurringView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    public static final String TAG = "locationCheck";

    private static final int[] COLORS = new int[]{R.color.primary_dark, R.color.primary, R.color.primary_light, R.color.accent, R.color.primary_dark_material_light};
    private static final LatLngBounds BOUNDS_TURKEY = new LatLngBounds(
            new LatLng(-85, -180), new LatLng(85, 180));
    //  double latitude, longitude;
    ArrayList<Route> allRoutes;
    //Google and Location Settings
    LocationManager locationManager;
    Location location;
    LatLng start, end;
    LatLng markerLatlng;
    //Search Area
    AutoCompleteTextView startPoint;
    AutoCompleteTextView endPoint;
    AutoCompleteAdapter autoCompleteAdapter;
    //variables
    double payment;
    private ProgressDialog progressDialog;
    private ImageView firstRouteButton;
    private ImageView secondRouteButton;
    private ImageView thirdRouteButton;
    private List<Polyline> polylines;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private BlurringView blurringView;

    //BotomBar Texts
    private TextView paymentText;
    private TextView distanceText;
    private TextView timeText;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initEvents initValues initView

        buildGoogleMap();
        blurringView = (BlurringView) findViewById(R.id.blurringView);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showSettingsAlert();
        }

        buildGoogleApiClient();

        mGoogleApiClient.connect();
        polylines = new ArrayList<>();

        MapsInitializer.initialize(getApplicationContext());

        createLocationRequest();

        blurMap();

        buildSearchArea();


    }

    private void buildSearchArea() {

        startPoint = (AutoCompleteTextView) findViewById(R.id.auto_complete_textView1);
        endPoint = (AutoCompleteTextView) findViewById(R.id.auto_complete_textView2);

        firstRouteButton = (ImageView) findViewById(R.id.firstRouteButton);
        secondRouteButton = (ImageView) findViewById(R.id.secondRouteButton);
        thirdRouteButton = (ImageView) findViewById(R.id.thirdRouteButton);

        paymentText = (TextView) findViewById(R.id.estimated_payment);
        distanceText = (TextView) findViewById(R.id.distance);
        timeText = (TextView) findViewById(R.id.estimated_time);

        firstRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firstRouteButton.setSelected(true);
                secondRouteButton.setSelected(false);
                thirdRouteButton.setSelected(false);

                drawPoliyline(allRoutes, 0);

                int payment = calculatePaymentInTL(allRoutes.get(0).getDistanceValue());

                paymentText.setText(String.valueOf(payment));
                distanceText.setText(allRoutes.get(0).getDistanceText());
                timeText.setText(allRoutes.get(0).getDurationText());


            }
        });

        secondRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firstRouteButton.setSelected(false);
                secondRouteButton.setSelected(true);
                thirdRouteButton.setSelected(false);

                drawPoliyline(allRoutes, 1);

                int payment = calculatePaymentInTL(allRoutes.get(1).getDistanceValue());

                paymentText.setText(String.valueOf(payment));
                distanceText.setText(allRoutes.get(1).getDistanceText());
                timeText.setText(allRoutes.get(1).getDurationText());


            }
        });

        thirdRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firstRouteButton.setSelected(false);
                thirdRouteButton.setSelected(true);
                secondRouteButton.setSelected(false);

                drawPoliyline(allRoutes, 2);

                int payment = calculatePaymentInTL(allRoutes.get(2).getDistanceValue());

                paymentText.setText(String.valueOf(payment));
                distanceText.setText(allRoutes.get(2).getDistanceText());
                timeText.setText(allRoutes.get(2).getDurationText());
            }
        });


        autoCompleteAdapter = new AutoCompleteAdapter(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, mGoogleApiClient, BOUNDS_TURKEY, null);

        startPoint.setAdapter(autoCompleteAdapter);
        endPoint.setAdapter(autoCompleteAdapter);

        startPoint.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                final AutoCompleteAdapter.PlaceAutocomplete item = autoCompleteAdapter.getItem(position);
                final String placeId = String.valueOf(item.placeId);

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);

                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {

                        if (!places.getStatus().isSuccess()) {

                            places.release();
                        }

                        final Place place = places.get(0);

                        start = place.getLatLng();

                    }
                });
            }
        });

        endPoint.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                final AutoCompleteAdapter.PlaceAutocomplete item = autoCompleteAdapter.getItem(position);
                final String placeId = String.valueOf(item.placeId);

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);

                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {

                        if (!places.getStatus().isSuccess()) {

                            places.release();
                        }

                        final Place place = places.get(0);

                        end = place.getLatLng();

                    }
                });

            }
        });

        startPoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (start != null) {
                    start = null;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        endPoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (end != null) {
                    end = null;
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    private void blurMap() {


        View blurredView = findViewById(R.id.bottom_barLayout);


        blurringView.setBlurredView(blurredView);

    }

    public void buildGoogleMap() {

        SupportMapFragment fragment = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);
        //       View mapView = fragment.getView();
//        View locationButton = ((View) mapView.findViewById(1).getParent()).findViewById(2);
/*
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.setMargins(0, 0, 30, 120);*/
        fragment.getMapAsync(this);

    }

    protected synchronized void buildGoogleApiClient() {

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }

    }

    protected void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void route() {

        if (start == null || end == null) {
            if (start == null) {
                if (startPoint.getText().length() > 0) {
                    startPoint.setError("Choose location from dropdown.");
                } else {
                    Toast.makeText(getApplicationContext(), "Please choose a starting point.", Toast.LENGTH_SHORT).show();
                }
            }
            if (end == null) {
                if (endPoint.getText().length() > 0) {
                    endPoint.setError("Choose location from dropdown.");
                } else {
                    Toast.makeText(getApplicationContext(), "Please choose a destination.", Toast.LENGTH_SHORT).show();
                }
            }
        } else

            progressDialog = ProgressDialog.show(getApplicationContext(), "Please wait.",
                    "Fetching route information.", true);
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(start, end)
                .build();
        routing.execute();
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
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_marker));
        map.addMarker(options);

        // End marker
        options = new MarkerOptions();
        options.position(end);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
        map.addMarker(options);

    }

    public void calculateRoute() {

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

        //    createLocationRequest();

        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null && map != null) {
            //        latitude = Double.valueOf(location.getLatitude());
            //        longitude = Double.valueOf(location.getLongitude());

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            start = latLng;
            //    map.addMarker(new MarkerOptions().position(latLng).title("hiyk"));


            CameraUpdate myLocation = CameraUpdateFactory.newLatLng(latLng);

            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

            map.moveCamera(myLocation);
            map.animateCamera(zoom);

        } else {
            Log.d(TAG, "(Couldn't get the location");
        }
    }

    private int calculatePaymentInTL(int distanceValue) {

        int kilometers = distanceValue / 1000;
        int meters = distanceValue - kilometers * 1000;

        payment = (3.45 + (kilometers * 2.10) + (meters * 0.0021));

        int returnVal = ((int) payment);

        return returnVal;

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
    public void onRoutingSuccess(ArrayList<Route> route, int whichRoute) {

        Log.d("locationTag", "onRoutingSuccess");

        allRoutes = new ArrayList<Route>(route);

//        Toast.makeText(getApplicationContext(), "Route " + (whichRoute + 1) + ": distance - " + route.get(whichRoute).getDistanceValue() + ": duration - " + route.get(whichRoute).getDurationValue(), Toast.LENGTH_LONG).show();

        double result = calculatePaymentInTL(route.get(whichRoute).getDistanceValue());


    }

    @Override
    public void onRoutingCancelled() {

        Log.d("locationTag", "onRoutingCancelled");

        Log.i(TAG, "Routing was cancelled.");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        displayLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = ");
        buildGoogleApiClient();
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
