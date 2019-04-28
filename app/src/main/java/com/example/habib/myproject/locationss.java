package com.example.habib.myproject;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.IgnoreExtraProperties;

import android.location.Geocoder;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.IOException;

@IgnoreExtraProperties
public class locationss {

    public double lat;
    public double lng;

    public locationss() {
    }

    public locationss(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }
}