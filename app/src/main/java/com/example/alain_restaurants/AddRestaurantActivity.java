package com.example.alain_restaurants;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.internal.location.zzas;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class AddRestaurantActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerDragListener {
    String gps_lat = "";
    String gps_long = "";
    GoogleMap googleMapa;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    Location currentLocation;

    Marker Marki;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_restaurant);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLoaction();

        Button btn_addRestaurantSubmit = findViewById(R.id.btn_AddRestaurantSubmit);
        Button btn_GetGPS = findViewById(R.id.btn_GetGPS);
        textView = findViewById(R.id.tv_Position);
        EditText et_RestaurantName = findViewById(R.id.et_RestaurantName);
        EditText et_RestaurantPhone = findViewById(R.id.et_RestaurantPhone);

        SharedPreferences preferences = getSharedPreferences("userPreferences", Activity.MODE_PRIVATE);

        String userId = preferences.getString("userId", "0");

        final String locationProvider = LocationManager.GPS_PROVIDER;
        final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        btn_addRestaurantSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = et_RestaurantName.getText().toString();
                String phone = et_RestaurantPhone.getText().toString();

                String url = "http://dev.imagit.pl/mobilne/api/restaurant/add";

                RequestParams params = new RequestParams();
                params.put("user",userId);
                params.put("name",name);
                params.put("phone",phone);
                params.put("lat",gps_lat);
                params.put("long",gps_long);

                AsyncHttpClient client = new AsyncHttpClient();
                client.post(url, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String response = new String(responseBody);

                        if (response.equals("OK")){
                            Toast.makeText(AddRestaurantActivity.this, R.string.restaurantAdded, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(AddRestaurantActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(AddRestaurantActivity.this, R.string.errorAPI, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                    }
                });
            }
        });

        btn_GetGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(AddRestaurantActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddRestaurantActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(locationProvider, 5000, 50, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        gps_lat= ""+location.getLatitude();
                        gps_long= ""+location.getLongitude();

                        textView.setText(gps_lat+" x "+gps_long);

                        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                        //MarkerOptions markerOptions= new MarkerOptions().position(latLng).title("My Location");
                        googleMapa.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        googleMapa.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                        googleMapa.clear();
                        MarkerOptions mark= new MarkerOptions().position(latLng).draggable(true).title("Please Drag to your desired restaurant location");
                        Marki = googleMapa.addMarker(mark);
                        Marki.showInfoWindow();
                        gps_lat = Marki.getPosition().latitude+"";
                        gps_long = Marki.getPosition().longitude+"";
                    }
                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle){

                    }
                    @Override
                    public void onProviderEnabled(String s){

                    }
                    @Override
                    public void onProviderDisabled(String s){

                    }
                });
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMapa = googleMap;
        googleMapa.setOnMapClickListener(this::onMapClick);
        googleMapa.setOnMarkerDragListener(this);

    }

    @Override
    public void onMapClick(LatLng latLng) {
        googleMapa.clear();
        MarkerOptions mark= new MarkerOptions().position(latLng).draggable(true).title("Please Drag to your desired restaurant location");
        Marki = googleMapa.addMarker(mark);
        Marki.showInfoWindow();
        gps_lat = Marki.getPosition().latitude+"";
        gps_long = Marki.getPosition().longitude+"";
        textView.setText(Marki.getPosition().latitude+" x "+Marki.getPosition().longitude);

    }
    private void fetchLastLoaction() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    currentLocation = location;
                    Toast.makeText(getApplicationContext(),currentLocation.getLatitude()
                            +" "+currentLocation.getLongitude(),Toast.LENGTH_SHORT).show();
                    SupportMapFragment supportMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
                    supportMapFragment.getMapAsync(AddRestaurantActivity.this::onMapReady);
                }
            }
        });
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        gps_lat = Marki.getPosition().latitude+"";
        gps_long = Marki.getPosition().longitude+"";
        textView.setText(Marki.getPosition().latitude+" x "+Marki.getPosition().longitude);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        gps_lat = Marki.getPosition().latitude+"";
        gps_long = Marki.getPosition().longitude+"";
        textView.setText(Marki.getPosition().latitude+" x "+Marki.getPosition().longitude);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        gps_lat = Marki.getPosition().latitude+"";
        gps_long = Marki.getPosition().longitude+"";
        textView.setText(Marki.getPosition().latitude+" x "+Marki.getPosition().longitude);
    }
}