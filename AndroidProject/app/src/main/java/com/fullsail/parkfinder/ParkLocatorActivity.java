package com.fullsail.parkfinder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParkLocatorActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ImageView imgSearch, btnMenu;
    private cPark p;
    private RecyclerView recyclerView;
    private MapView mapView;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double userLat;
    private double userLng;
    private final long Min_Time = 1000;//1 seconds
    private final long Min_dist = 5; //5 meters
    private Uri.Builder builder;
    private RequestQueue requestQueue;
    private String url;
    private JSONArray jsonArray;
    private List<cPark> parks;
    private List<ParkDistance> nearbyParks;
    private int radius, parksToDisplay;
    private String zipCode;
    private boolean isFiltered = false;
    private FirebaseAuth fAuth;
    private NotificationManagerCompat notificationManager;
    private FirebaseDatabase database;
    private DatabaseReference mdatabase;
    private boolean alreadyExcuted = false;
    private boolean zipCodeSearch = false;
    private double zipLat, zipLng;
    private GoogleMap mMap;
    private TextView noResults;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.park_locator);

        btnMenu = findViewById(R.id.btnMenu);
        imgSearch = findViewById(R.id.imgSearch);
        p = new cPark();
        recyclerView = findViewById(R.id.recyclerview);
        parks = new ArrayList<>();
        nearbyParks = new ArrayList<>();
        noResults = findViewById(R.id.noresults);
        sharedPreffs();
        radius = 50;
        parksToDisplay = 3;
        fAuth = FirebaseAuth.getInstance();
        mdatabase= FirebaseDatabase.getInstance().getReference();
        notificationManager = NotificationManagerCompat.from(this);

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ParkLocatorActivity.this, MainMenuActivity.class));
            }
        });

        initMapView(savedInstanceState);
        checkPermissionMap();
        getParkInformation();

        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ParkLocatorActivity.this, SearchFiltersActivity.class));
            }
        });
    }

    private void checkPermissionMap() {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                Toast.makeText(ParkLocatorActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_COARSE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                Toast.makeText(ParkLocatorActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();

    }

    private void initMapView(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;

        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();


        Intent i = getIntent();
        p = (cPark) i.getSerializableExtra("park");

        if(i.getSerializableExtra("filteredParkList") != null) {
            parks = (ArrayList<cPark>) i.getSerializableExtra("filteredParkList");

            String r = i.getStringExtra("radius");

            if (r.equals("")) {
                radius = 50;
            } else {
                radius = Integer.parseInt(r);
            }

            String disp = i.getStringExtra("displayCount");

            if (disp.equals("")) {
                parksToDisplay = 3;
            } else {
                parksToDisplay = Integer.parseInt(disp);
            }

            zipCode = i.getStringExtra("zipCode");

            if (zipCode.length() == 5) {
                zipCodeSearch = true;
                getZipLatLng();
            }

            isFiltered = true;
        }
        
        if (p != null) {
            RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, p);
            recyclerView.setAdapter(adapter);
        }
        else{

            List<cPark> temp = new ArrayList<>();

            for (int j = 0; j < nearbyParks.size(); j++) {
                temp.add(nearbyParks.get(j).p);
            }

            RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, temp);
            recyclerView.setAdapter(adapter);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

     @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
            mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                userLat = location.getLatitude();
                userLng = location.getLongitude();

                if (nearbyParks.isEmpty()) {
                    FindParksNearby();
                }

                if (p != null) {
                    LatLng parkLatLong = new LatLng(p.getLat(), p.getLng());
                    mMap.addMarker(new MarkerOptions().position(parkLatLong).title(p.getFullName()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(parkLatLong, 6.5f));
                }
//                else if (!nearbyParks.isEmpty()){
//                    for (int i = 0; i < nearbyParks.size(); i++) {
//                        LatLng parkLatLong = new LatLng(nearbyParks.get(i).p.getLat(), nearbyParks.get(i).p.getLng());
//                        mMap.addMarker(new MarkerOptions().position(parkLatLong).title(nearbyParks.get(i).p.getFullName()));
//                    }
//                }

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(userLat, userLng))
                        .title("User Position")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))).setVisible(false);

                if (zipCodeSearch) {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(zipLat, zipLng))
                            .title(zipCode)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))).setVisible(true);
                }

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }

                mMap.setMyLocationEnabled(true);

                if(p == null && !zipCodeSearch) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLat, userLng), 6.5f));
                } else if (p == null && zipCodeSearch) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(zipLat, zipLng), 6.5f));
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }


        };
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Min_Time, Min_dist, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void getParkInformation() {
        DiskBasedCache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());

        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();

        builder = new Uri.Builder();
        builder.scheme("https").authority("developer.nps.gov").appendPath("api").appendPath("v1")
                .appendPath("parks");

        builder.appendQueryParameter("api_key", "qYfJXGgu3oc42jxLtI08WTqcGhfWsWRh0a60EYe8");
        builder.appendQueryParameter("limit", "1000");


        url = builder.build().toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            jsonArray = response.getJSONArray("data");
                            int results = jsonArray.length();

                            for (int i = 0; i < results; i++) {
                                JSONObject parkinfo;

                                if (jsonArray.getJSONObject(i) == null)
                                    break;
                                else
                                    parkinfo = jsonArray.getJSONObject(i);

                                String name = parkinfo.getString("fullName");
                                double lat = (double) parkinfo.getDouble("latitude");
                                double lng = (double) parkinfo.getDouble("longitude");
                                String parkCode = parkinfo.getString("parkCode");
                                String des = parkinfo.getString("designation");

                                JSONArray jsonImages = parkinfo.getJSONArray("images");
                                List<String> imageURLs = new ArrayList<>();

                                for (int j = 0; j < jsonImages.length(); j++) {
                                    JSONObject u = jsonImages.getJSONObject(j);
                                    imageURLs.add(u.getString("url"));
                                }

                                cPark park = new cPark();
                                park.setFullName(name);
                                park.setLat(lat);
                                park.setLng(lng);
                                park.setParkCode(parkCode);
                                park.setDesignation(des);
                                park.setImages(imageURLs);

                                if (!isFiltered) {
                                    parks.add(park);
                                } else {
                                    break;
                                }
                            }

                            if (!isFiltered) {
                                FindParksNearby();
                            }

                        } catch (JSONException e) {
//                            textView.setText("JSON error");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //textView.setText("NO");

                    }
                });
        RequestSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void FindParksNearby() {

        for (int i = 0; i < parks.size(); i++) {
            double dist = DistanceFromLocation(parks.get(i).getLat(), parks.get(i).getLng());
            if (dist < radius) {
                nearbyParks.add(new ParkDistance(parks.get(i), dist));
            }
        }

        // Display closest parks
        for (int i = 0; i < nearbyParks.size(); i++) {
            ParkDistance min = nearbyParks.get(i);
            int minId = i;
            for (int j = i+1; j < nearbyParks.size(); j++) {
                if (nearbyParks.get(j).getDistFromLocation() < min.getDistFromLocation()) {
                    min = nearbyParks.get(j);
                    minId = j;
                }
            }

            ParkDistance temp = nearbyParks.get(i);
            nearbyParks.set(i, min);
            nearbyParks.set(minId, temp);
        }

        if (nearbyParks.size() > parksToDisplay) {
            nearbyParks.subList(parksToDisplay, nearbyParks.size()).clear();
        }

        if (p == null) {
            List<cPark> temp = new ArrayList<>();

            for (int i = 0; i < nearbyParks.size(); i++) {
                temp.add(nearbyParks.get(i).p);
            }

            if (temp.size() > 0) {
                noResults.setVisibility(View.INVISIBLE);
                RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, temp);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
            } else {
                noResults.setVisibility(View.VISIBLE);
            }
        } else {
            noResults.setVisibility(View.INVISIBLE);
            RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, p);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }


        for (int i = 0; i < nearbyParks.size(); i++) {
            LatLng parkLatLong = new LatLng(nearbyParks.get(i).p.getLat(), nearbyParks.get(i).p.getLng());
            mMap.addMarker(new MarkerOptions().position(parkLatLong).title(nearbyParks.get(i).p.getFullName()));
        }
    }

    private double DistanceFromLocation(double parkLat, double parkLng) {

        double lat, lng;

        if (zipCodeSearch) {
            lat = zipLat;
            lng = zipLng;
        } else {
            lat = userLat;
            lng = userLng;
        }

        double dLat = Math.toRadians(parkLat - lat);
        double dLng = Math.toRadians(parkLng - lng);

        double tempCurrentLat = Math.toRadians(lat);
        parkLat = Math.toRadians(parkLat);

        double a = Math.pow(Math.sin(dLat / 2) , 2) +
                Math.pow(Math.sin(dLng / 2), 2) *
                        Math.cos(tempCurrentLat) * Math.cos(parkLat);

        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        double d = rad * c;
        return d * 0.62;
    }

    private void getZipLatLng() {
        final Geocoder geocoder = new Geocoder(this);

        try {
            List<Address> addresses = geocoder.getFromLocationName(zipCode, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                zipLat = address.getLatitude();
                zipLng = address.getLongitude();
            } else {
                Toast.makeText(this, "Geocoder failed", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ParkDistance {
        public cPark p;
        public double distFromLocation;

        public ParkDistance(cPark p, double distFromLocation) {
            this.p = p;
            this.distFromLocation = distFromLocation;
        }

        public double getDistFromLocation() {
            return distFromLocation;
        }
    }

    private void sharedPreffs()
    {
        SharedPreferences sharedPreferences
                = getSharedPreferences(
                "sharedPrefs", MODE_PRIVATE);
        final SharedPreferences.Editor editor
                = sharedPreferences.edit();
        final boolean isDarkModeOn
                = sharedPreferences
                .getBoolean(
                        "isDarkModeOn", false);




        // When user reopens the app
        // after applying dark/light mode
        if (isDarkModeOn) {
            AppCompatDelegate
                    .setDefaultNightMode(
                            AppCompatDelegate
                                    .MODE_NIGHT_YES);

        }
        else {
            AppCompatDelegate
                    .setDefaultNightMode(
                            AppCompatDelegate
                                    .MODE_NIGHT_NO);

        }





    }






}

