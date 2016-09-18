package com.example.haticesigirci.payment;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Created by haticesigirci on 15/09/16.
 */
public class AutoCompleteAdapter extends ArrayAdapter<AutoCompleteAdapter.PlaceAutoComplete> implements Filterable {


    //Definitions
    private final GoogleApiClient googleApiClient;
    private ArrayList<PlaceAutocomplete> resultList;
    private LatLngBounds bounds;
    private final AutocompleteFilter placeFilter;

    public AutoCompleteAdapter(Context context, int resource, GoogleApiClient googleApiClient, LatLngBounds latLngBounds, AutocompleteFilter filter) {
        super(context, resource);

        this.googleApiClient = googleApiClient;
        this.bounds = latLngBounds;
        this.placeFilter = filter;


    }


    public void setBounds(LatLngBounds bounds) {
        this.bounds = bounds;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public PlaceAutoComplete getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();

                if (constraint != null) {

                    resultList = getAutoComplete(constraint.toString());
                    Log.d("insideIfconstraint","insideIfConstraint");
                   // if (resultList != null) {

                        Log.d("insideIfresult","insideIfresult");

                        results.count = resultList.size();
                        results.values = resultList;
                   // }

                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

                if (filterResults != null && filterResults.count > 0) {

                    notifyDataSetChanged(); //update data

                } else {

                    notifyDataSetInvalidated(); //invalidate data set
                }

            }
        };

        return filter;
    }

    private ArrayList<PlaceAutocomplete> getAutoComplete(CharSequence constraint) {

        if (googleApiClient.isConnected()) {


            PendingResult<AutocompletePredictionBuffer> results = Places.GeoDataApi.getAutocompletePredictions(googleApiClient, constraint.toString(), bounds, placeFilter);

            AutocompletePredictionBuffer autocompletePredictions = results.await(60, TimeUnit.SECONDS); //wait for 60 seconds to find that is searched item

            final Status status = autocompletePredictions.getStatus(); // check result

            if (!status.isSuccess()) {

                autocompletePredictions.release(); //break

                return null;
            }

            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());

            while (iterator.hasNext()) {

                AutocompletePrediction prediction = iterator.next();
                resultList.add(new PlaceAutoComplete(prediction.getPlaceId(), prediction.getFullText(null)));

            }

            autocompletePredictions.release();

            return resultList;

        }

        return null;
    }

    class PlaceAutoComplete {

        public CharSequence placeId;
        public CharSequence description;

        PlaceAutoComplete(CharSequence placeId, CharSequence description) {

            this.placeId = placeId;
            this.description = description;

        }

        @Override
        public String toString() {
            return description.toString();
        }
    }
}
