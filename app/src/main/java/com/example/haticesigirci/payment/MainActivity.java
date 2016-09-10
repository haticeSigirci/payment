package com.example.haticesigirci.payment;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    FragmentTransaction ft;
    GoogleMapFragment mapFragment;
    Bundle args;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapFragment = new GoogleMapFragment();


        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, mapFragment);
        ft.commit();

    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

}
