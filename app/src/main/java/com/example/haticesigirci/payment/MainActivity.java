package com.example.haticesigirci.payment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class MainActivity extends AppCompatActivity {


    FragmentTransaction ft;
    GoogleMapFragment mapFragment;
    Bundle args;
    FragmentPagerItemAdapter adapter;
    SearchAreaFragment searchAreaFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(R.string.google_map_title, GoogleMapFragment.class)
                .add(R.string.choose_title, SearchAreaFragment.class)
                .create());

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        viewPagerTab.setViewPager(viewPager);


        mapFragment = new GoogleMapFragment();

        searchAreaFragment = new SearchAreaFragment();


        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, mapFragment);
        ft.replace(R.id.fragment, searchAreaFragment);
        ft.commit();

    }

    public void onPageSelected(int position) {

        //.instantiateItem() from until .destoryItem() is called it will be able to get the Fragment of page.
        Fragment page = adapter.getPage(position);


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
