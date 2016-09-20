package com.example.haticesigirci.payment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

/**
 * Created by haticesigirci on 13/09/16.
 */
public class SearchAreaFragment extends Fragment implements RoutingListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final LatLngBounds BOUNDS_TURKEY = new LatLngBounds(
            new LatLng(-85, -180), new LatLng(85, 180));

    //   @InjectView(R.id.start_point)
    AutoCompleteTextView startPoint;
    //   @InjectView(R.id.destination_point)
    AutoCompleteTextView endPoint;
    PlaceAutocomplete placeAutocomplete;
    LatLng start;
    LatLng end;
    AutoCompleteAdapter autoCompleteAdapter;
    double payment;
    TextView estimatedPayment;
    TextView estimatedTime;
    TextView distance;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location location;
    private Button routeButton;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, null, false);


        startPoint = (AutoCompleteTextView) view.findViewById(R.id.start_point);
        endPoint = (AutoCompleteTextView) view.findViewById(R.id.destination_point);
        routeButton = (Button) view.findViewById(R.id.btnUpload);


        distance = (TextView) view.findViewById(R.id.distance);
        estimatedPayment = (TextView) view.findViewById(R.id.estimated_payment);
        estimatedTime = (TextView) view.findViewById(R.id.estimated_time);

        Context c = getActivity().getApplicationContext();

        //  ButterKnife.inject(getActivity());

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        createLocationRequest();


        autoCompleteAdapter = new AutoCompleteAdapter(c, android.R.layout.simple_dropdown_item_1line, mGoogleApiClient, BOUNDS_TURKEY, null);

        startPoint.setAdapter(autoCompleteAdapter);
        endPoint.setAdapter(autoCompleteAdapter);
        startPoint.requestFocus();


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

                Log.d("beforeText", "insideBeforeTextChanged");

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                Log.d("insideOnText", "insideOntextchanged");

                if (start != null) {
                    start = null;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {


                Log.d("afterText", "insideBeforeTextChanged");
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


        routeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("insidebutton", "insidebutton");
                route();
            }
        });


        Log.d("understandingbutton", "outside");


        return view;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        }

        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


        Log.d("locationCheck2", String.valueOf(location.getLatitude()));
        Log.d("locationCheck2", String.valueOf(location.getLongitude()));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();

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
                    Toast.makeText(getContext(), "Please choose a starting point.", Toast.LENGTH_SHORT).show();
                }
            }
            if (end == null) {
                if (endPoint.getText().length() > 0) {
                    endPoint.setError("Choose location from dropdown.");
                } else {
                    Toast.makeText(getContext(), "Please choose a destination.", Toast.LENGTH_SHORT).show();
                }
            }
        } else

            progressDialog = ProgressDialog.show(getContext(), "Please wait.",
                    "Fetching route information.", true);
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(start, end)
                .build();
        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int whichRoute) {

        progressDialog.dismiss();

        Toast.makeText(getContext(), "Route " + (whichRoute + 1) + ": distance - " + route.get(whichRoute).getDistanceValue() + ": duration - " + route.get(whichRoute).getDurationValue(), Toast.LENGTH_LONG).show();

        double result = calculatePaymentInTL(route.get(whichRoute).getDistanceValue());


        estimatedPayment.setText(String.valueOf(result));
        estimatedTime.setText(String.valueOf(route.get(whichRoute).getDurationValue()));
        distance.setText(String.valueOf(route.get(whichRoute).getDistanceValue()));


    }

    private double calculatePaymentInTL(int distanceValue) {

        int kilometers = distanceValue / 1000;
        int meters = distanceValue - kilometers * 1000;

        payment = 3.45 + (distanceValue * 2.10) + (meters * 0.0021);

        return payment;

    }

    @Override
    public void onRoutingCancelled() {

    }
}




