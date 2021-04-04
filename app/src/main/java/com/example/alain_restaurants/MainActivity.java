package com.example.alain_restaurants;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

//Maps
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static java.lang.Double.parseDouble;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap googleMapa;
    private static final String TAG = "MainActivity";
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;

    final ArrayList<String> restaurantsList = new ArrayList<String>();
    final ArrayList<String> restaurantsListIDs = new ArrayList<String>();
    //final ArrayList<String> restaurantsListLong = new ArrayList<String>();
    //final ArrayList<String> restaurantsListLat = new ArrayList<String>();
    int prevClick;

    final ArrayList<LatLng> arrayList= new ArrayList<LatLng>();

    public MainActivity() {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMapa = googleMap;
        LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        //MarkerOptions markerOptions= new MarkerOptions().position(latLng).title("My Location");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        //googleMap.addMarker(new MarkerOptions().position(latLng).title("My Location"));


        //for(int i = 0 ; i < arrayList.size(); i++) {
            //if (restaurantsListLat.get(i)!="" || restaurantsListLong.get(i) !=""){
            //googleMap.addMarker(new MarkerOptions().position(arrayList.get(i)).title(restaurantsList.get(i)));
            //Toast.makeText(MainActivity.this, restaurantsList.get(i), Toast.LENGTH_SHORT).show();
            //}
        //}

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLoaction();

        Button btn_AddRestaurant = findViewById(R.id.btn_AddRestaurant);

        final ListView lv_Restaurants = (SwipeMenuListView) findViewById(R.id.lv_Restaurants);
        SharedPreferences preferences = getSharedPreferences("userPreferences", Activity.MODE_PRIVATE);
        String userId = preferences.getString("userId", "0");

        final ArrayAdapter<String> restaurantsAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, restaurantsList);
        lv_Restaurants.setAdapter(restaurantsAdapter);

        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://dev.imagit.pl/mobilne/api/restaurants/" + userId;

        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String JSON = new String(responseBody);
                try {
                    JSONArray jArray = new JSONArray(JSON);
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject jObject = jArray.getJSONObject(i);

                        String restaurantName = jObject.getString("RESTAURANT_NAME");
                        String restaurantPhone = jObject.getString("RESTAURANT_PHONE");
                        String restautantId = jObject.getString("RESTAURANT_ID");
                        String restautantLat = jObject.getString("RESTAURANT_LAT");
                        String restautantLong = jObject.getString("RESTAURANT_LONG");

                        restaurantsList.add(restaurantName + ", " + restaurantPhone);
                        restaurantsListIDs.add(restautantId);
                        //restaurantsListLat.add(restautantLat);
                        //restaurantsListLong.add(restautantLong);

                        arrayList.add(new LatLng(parseStringToDouble(restautantLat,0.00),parseStringToDouble(restautantLong,0.00)));
                    }
                    lv_Restaurants.setAdapter(restaurantsAdapter);
                    lv_Restaurants.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Toast.makeText(MainActivity.this, restaurantsList.toArray()[position].toString(), Toast.LENGTH_SHORT).show();
                            googleMapa.clear();
                            googleMapa.addMarker(new MarkerOptions().position(arrayList.get(position)).title(restaurantsList.get(position)));
                           //mMap.animateCamera(CameraUpdateFactory.newLatLng(arrayList.get(position)));
                           //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(arrayList.get(position), 9));

                            googleMapa.animateCamera(CameraUpdateFactory.newLatLng(arrayList.get(position)));
                            googleMapa.animateCamera(CameraUpdateFactory.newLatLngZoom(arrayList.get(position), 17));
                        }
                    });


                    //SWIPE MENU CREATOR START
                    SwipeMenuCreator creator = new SwipeMenuCreator() {

                        @Override
                        public void create(SwipeMenu menu) {
                            // create "open" item
                            SwipeMenuItem openItem = new SwipeMenuItem(
                                    getApplicationContext());
                            // set item background
                            //openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                            //0xCE)));
                            // set item width
                            //openItem.setWidth(180);
                            // set item title
                            //openItem.setTitle("Open");
                            // set item title fontsize
                            //openItem.setTitleSize(18);
                            // set item title font color
                            openItem.setTitleColor(Color.WHITE);
                            // add to menu
                            menu.addMenuItem(openItem);

                            // create "delete" item
                            SwipeMenuItem deleteItem = new SwipeMenuItem(
                                    getApplicationContext());
                            // set item background
                            deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                            // set item width
                            deleteItem.setWidth(200);
                            // set a icon
                            deleteItem.setIcon(R.drawable.ic_delete);
                            // add to menu
                            menu.addMenuItem(deleteItem);
                        }
                    };

                    // set creator
                    ((SwipeMenuListView) lv_Restaurants).setMenuCreator(creator);
                    //SWIPE MENU CREATOR START
                    ((SwipeMenuListView) lv_Restaurants).setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                            switch (index) {
                                case 0:
                                    // open
                                    break;
                                case 1:
                                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    //Yes button clicked
                                                    String url = "http://dev.imagit.pl/mobilne/api/restaurant/delete/" + userId + "/" + restaurantsListIDs.toArray()[position];
                                                    client.get(url, new AsyncHttpResponseHandler() {
                                                        @Override
                                                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                            Toast.makeText(MainActivity.this, restaurantsList.toArray()[position].toString() + " deleted successfully", Toast.LENGTH_SHORT).show();
                                                            restaurantsList.remove(position);
                                                            restaurantsListIDs.remove(position);
                                                            //restaurantsListLat.remove(position);
                                                            //restaurantsListLong.remove(position);
                                                            arrayList.remove(position);
                                                            restaurantsAdapter.notifyDataSetChanged();
                                                            googleMapa.clear();
                                                            onMapReady(googleMapa);
                                                        }

                                                        @Override
                                                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                                        }
                                                    });


                                                    //Toast.makeText(MainActivity.this,restaurantsList.toArray()[position].toString(), Toast.LENGTH_SHORT).show();
                                                    break;

                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    //No button clicked
                                                    break;
                                            }
                                        }
                                    };

                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setMessage("Are you sure you want to delete " + restaurantsList.toArray()[position].toString()).setPositiveButton("Yes DELETE", dialogClickListener)
                                            .setNegativeButton("No", dialogClickListener).show();
                                    break;
                            }
                            // false : close the menu; true : not close the menu
                            return false;
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
        btn_AddRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddRestaurantActivity.class);
                startActivity(intent);
            }
        });
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
                    supportMapFragment.getMapAsync(MainActivity.this::onMapReady);
                }
            }
        });
    }


    private static double parseStringToDouble(String value, double defaultValue) {
        return value == null || value.isEmpty() ? defaultValue : Double.parseDouble(value);
    }


}