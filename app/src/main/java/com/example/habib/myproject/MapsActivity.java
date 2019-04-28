package com.example.habib.myproject;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    List<Address> theAddress = null;
    DatabaseReference reference;
    DatabaseReference referenceChild;
    FirebaseDatabase database;
    LocationManager locationManager;
    private double lat;
    private double lng;
    private double end_latitude;
    private double end_longitude;
    int username;
    int usernameText;
    View view;
    Marker marker;
    Marker otherMarker;
    locationss locations;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        polylines = new ArrayList<>();
        //===========================================================================================
        showAlertDialog(view);
        SearchDialog();
        erasePolyline();
        //===========================================================================================
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        database = FirebaseDatabase.getInstance();
        reference = database.getReferenceFromUrl("https://myproject-4e07e.firebaseio.com/");
        //===========================================================================================
        if (service_activity() && Nprovider()) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 5, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                        theLocation(lat, lng, 15);

                        // Send data to Firebase
                        locations = new locationss(lat, lng);
                        reference.child(String.valueOf(username)).setValue(locations);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                });

            }
        } //=========================================================================================
        else if (service_activity() && GPSprovider()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                    theLocation(lat, lng, 15);

                    // Send data to firebase
                    locations = new locationss(lat, lng);
                    reference.child(String.valueOf(username)).setValue(locations);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        /*if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }*/
    }

    // ============================== Get the Current Location ======================================
    public void theLocation(double lat, double lng, float zoom) {
        LatLng location = new LatLng(lat, lng);
        Geocoder getsLocation = new Geocoder(this);
        try {
            theAddress = getsLocation.getFromLocation(lat, lng, 1);
            String addressName = theAddress.get(0).getLocality() + "," + theAddress.get(0).getAdminArea()
                    + "," + theAddress.get(0).getThoroughfare() + "," + theAddress.get(0).getCountryName();
            Toast.makeText(this, addressName, Toast.LENGTH_LONG).show();

            if (marker != null) {
                marker.remove();
            }
            marker = mMap.addMarker(new MarkerOptions().position(location).title("Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //================================== Cheack for google service ==================================
    public boolean service_activity() {
        GoogleApiAvailability api_availability = GoogleApiAvailability.getInstance();
        //verifying that the Google Play services APK is available and up-to-date on this device.
        int isAPIthere = api_availability.isGooglePlayServicesAvailable(this);
        //Verifies that Google Play services is installed and enabled on this device.
        if (isAPIthere == ConnectionResult.SUCCESS) {
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
            return true;
        } else if (api_availability.isUserResolvableError(isAPIthere)) {
            //Determines whether an error can be resolved via user action.
            Dialog theDialog = api_availability.getErrorDialog(this, isAPIthere, 0);
            theDialog.show();
            return false;
        } else {
            Toast.makeText(this, "isn't connected to google play service", Toast.LENGTH_SHORT).show();
            // A toast provides simple feedback about an operation in a small popup.
            // Duration >> LENGTH_SHORT >> Show the view or text notification for a long period of time.
            return false;
        }
    }
    // =========================== check if NETWORK_PROVIDER is Enabled =============================
    public boolean Nprovider() {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    // ============================= check if GPS_PROVIDER is Enabled ===============================
    public boolean GPSprovider() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    //============================ Alert Dialog for Username =============================
    public void showAlertDialog(View view) {
        // generating Random username
        Random random = new Random();
        username = 100000 + random.nextInt(900000);

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Important Notification");
        // userName which is randomly generated
        builder.setMessage("Your Current "  + "Username: " + username);

        // add a button
        builder.setPositiveButton("DONE", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //========================== Text Alert Dialog for Share Location ===============================
    public void SearchDialog() {
        Button shareLoc = (Button) findViewById(R.id.shareLoc);

        shareLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View viewthis = (LayoutInflater.from(MapsActivity.this)).inflate(R.layout.input_user, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setView(viewthis);

                final EditText inputUsername = (EditText) viewthis.findViewById(R.id.username);

                builder.setCancelable(true).setPositiveButton("SHARE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        usernameText = Integer.parseInt(inputUsername.getText().toString());
                        GetDataFromFirebase();
                    }
                });

                Dialog dialog = builder.create();
                dialog.show();
            }
        });
    }
    //======================== Get data of other User or send Error Dialog ==========================
    public void GetDataFromFirebase() {
        referenceChild = reference.child(String.valueOf(usernameText));
        referenceChild.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                end_latitude = dataSnapshot.child("lat").getValue(Double.class);
                end_longitude = dataSnapshot.child("lng").getValue(Double.class);

                LatLng otherlocation = new LatLng(end_latitude, end_longitude);
                if (otherMarker != null)
                    otherMarker.remove();

                otherMarker = mMap.addMarker(new MarkerOptions().position(otherlocation).title("Other Location").
                        icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(otherlocation, 15));

                getRouteBetweenMarkers();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Database Error", databaseError.getMessage());
            }
        });
    }
//============================== Get Distance, Duration and Draw Route ==============================
    public void getRouteBetweenMarkers(){
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(lat,lng), new LatLng(end_latitude,end_longitude))
                .build();
        routing.execute();

    }

    public void erasePolyline(){
        Button endshareloc = (Button) findViewById(R.id.endshareloc);

        endshareloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Polyline line : polylines)
                    line.remove();

                polylines.clear();
                otherMarker.remove();
            }
        });
    }

    @Override
    public void onRoutingFailure(RouteException e) { // Mainly: Direction API Error
            Toast.makeText(this, "Route Error: Something went wrong, Try again", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingStart() {
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> routee, int shourtestRouteindex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines)
                poly.remove();
        }
        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <routee.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(routee.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"distance: "+ routee.get(i).getDistanceValue()+"- duration: "+ routee.get(i).getDurationValue(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRoutingCancelled() {
    }
}