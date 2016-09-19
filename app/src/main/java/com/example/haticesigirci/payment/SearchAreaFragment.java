package com.example.haticesigirci.payment;

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

/**
 * Created by haticesigirci on 13/09/16.
 */
public class SearchAreaFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final LatLngBounds BOUNDS_TURKEY = new LatLngBounds(
            new LatLng(-85, -180), new LatLng(85, 180));

    //   @InjectView(R.id.start_point)
    AutoCompleteTextView startPoint;
    //   @InjectView(R.id.destination_point)
    AutoCompleteTextView endPoint;
    PlaceAutocomplete placeAutocomplete;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location location;

    private Button routeButton;

    LatLng start;
    LatLng end;

    AutoCompleteAdapter autoCompleteAdapter;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, null, false);


        startPoint = (AutoCompleteTextView) view.findViewById(R.id.start_point);
        endPoint = (AutoCompleteTextView) view.findViewById(R.id.destination_point);
        routeButton = (Button) view.findViewById(R.id.btnUpload);


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


                if (position == 0) {

                    Log.d("understandingbutton", "inside");


                } else if (position == 2) {

                    Log.d("understandingbutton", "inside");

                }


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
}
