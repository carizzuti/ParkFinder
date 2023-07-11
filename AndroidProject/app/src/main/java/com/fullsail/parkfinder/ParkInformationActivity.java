package com.fullsail.parkfinder;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParkInformationActivity extends AppCompatActivity implements OnMapReadyCallback{
    private Button btnMore, btnLandmarkList;
    private TextView txtName, txtDescription;
    private ImageView btnBack;
    private cPark p;
    private Uri.Builder builder;
    private RequestQueue requestQueue;
    private String url, uid;
    private JSONArray jsonArray;
    private String parkCode;
    private DatabaseReference mdatabase;
    private FirebaseAuth fAuth;
    private CheckBox visited;
    private boolean isAdded = false;
    private boolean isRemoved = false;
    private boolean isStarted = true;
    private ArrayList<String> parkcodeArr;
    private MapView mapViewPark;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double userLat;
    private double userLng;
    private final long Min_Time = 1000;//1 seconds
    private final long Min_dist = 5; //5 meters
    private ArrayList<String> placeIds;
    private final String PARKS_PLACES_ID = "2856392B-17A7-4413-8521-C13A8D2188EB";
    private final String API_KEY = "qYfJXGgu3oc42jxLtI08WTqcGhfWsWRh0a60EYe8";
    private ArrayList<cPlace> places;
    private ArrayList<cPlace> visitorCenters;
    private ArrayList<cPlace> campgrounds;
    private ArrayList<cPlace> allPlaces;
    private ArrayList<cPlace> allCenters;
    private GoogleMap mMap;
    private ImageView parkBack;
    private boolean fromLocator = false;
    private boolean fromVisited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.park_information);

        btnMore = findViewById(R.id.btnMore);
        btnBack = findViewById(R.id.back);
        txtName = findViewById(R.id.parkName);
        txtDescription = findViewById(R.id.description);
        mdatabase= FirebaseDatabase.getInstance().getReference();
        fAuth = FirebaseAuth.getInstance();
        visited = findViewById(R.id.checkVisited);
        btnLandmarkList = findViewById(R.id.btnLandmarks);
        parkcodeArr = new ArrayList<>();
        placeIds = new ArrayList<>();
        places = new ArrayList<>();
        campgrounds = new ArrayList<>();
        visitorCenters = new ArrayList<>();
        initMapView(savedInstanceState);
        parkBack = (ImageView) findViewById(R.id.parkImageBlur);

        p = new cPark();

        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ParkInformationActivity.this, MoreInformationActivity.class);
                intent.putExtra("park", p);
                intent.putExtra("uid", uid);
                intent.putExtra("fromLocator", fromLocator);
                intent.putExtra("fromVisited", fromVisited);
                startActivity(intent);
            }
        });

        btnLandmarkList.setOnClickListener(new View.OnClickListener() {
            Intent intent = new Intent(ParkInformationActivity.this, LandmarkListActivity.class);
            ArrayList<String> checkedLandmarks;

            @Override
            public void onClick(View view) {
                DatabaseReference ref = mdatabase.child("/LandmarkList").child(fAuth.getCurrentUser().getUid()).child(p.getParkCode());
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.child("landmarks").exists()) {
                                String landmarks = snapshot.child("landmarks").getValue().toString();
                                checkedLandmarks = new ArrayList<String>(Arrays.asList(landmarks.split(",")));
                                intent.putExtra("checkedLandmarks", checkedLandmarks);
                            }
                            startActivity(intent);
                        } else {
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

                intent.putExtra("landmarkList", allPlaces);
                intent.putExtra("visitorCenterList", allCenters);
                intent.putExtra("campgroundsList", campgrounds);
                intent.putExtra("park", p);
                intent.putExtra("fromLocator", fromLocator);
                intent.putExtra("fromVisited", fromVisited);

                if (fromVisited) {
                    intent.putExtra("uid", uid);
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fromLocator) {
                    Intent intent = new Intent(ParkInformationActivity.this, ParkLocatorActivity.class);
                    startActivity(intent);
                } else if (fromVisited) {
                    Intent intent = new Intent(ParkInformationActivity.this, ParksVisitedActivity.class);
                    intent.putExtra("uid", uid);
                    startActivity(intent);
                } else {
                    onBackPressed();
                }
            }
        });

        DatabaseReference ref3 = mdatabase.child("/Profile/VisitedList").child(fAuth.getCurrentUser().getUid());
        ref3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                visited.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!snapshot.child("parkCodes").exists()){
                            if(isChecked) {
                                nearbyNotifications();
                            }
                        }
                        else if (snapshot.child("parkCodes").exists() && !isStarted)
                        {
                            if (isChecked){
                                nearbyNotifications();
                            }
                            else{
                                removeNearby();

                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void initMapView(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;

        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapViewPark = findViewById(R.id.map_viewInfo);
        mapViewPark.onCreate(mapViewBundle);
        mapViewPark.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapViewPark.onStart();

        Intent i = getIntent();
        p = (cPark) i.getSerializableExtra("park");
        parkCode = i.getStringExtra("code");
        fromLocator = i.getBooleanExtra("fromLocator", false);
        fromVisited = i.getBooleanExtra("fromVisited", false);
        uid = i.getStringExtra("uid");

        if (fromVisited) {
            visited.setEnabled(false);
        }

        if (allCenters != null) {
            allCenters.clear();
        }

        if (allPlaces != null) {
            allPlaces.clear();
        }

        if (campgrounds != null) {
            campgrounds.clear();
        }

        getParkInformation();
        Checkpark();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapViewPark.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapViewPark.onResume();
    }

        
    @Override
    public void onStop() {
        super.onStop();
        mapViewPark.onStop();
    }

    @Override
    public void onPause() {
        mapViewPark.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapViewPark.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapViewPark.onLowMemory();
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

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(userLat, userLng))
                        .title("User Position")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))).setVisible(false);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }

                mMap.setMyLocationEnabled(true);

                LatLng parkLatLong = new LatLng(p.getLat(), p.getLng());
                mMap.addMarker(new MarkerOptions().position(parkLatLong).title(p.getFullName())).setVisible(false);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(parkLatLong, 10.0f));

                //getParkInformation();
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

        builder.appendQueryParameter("api_key", API_KEY);

        if (parkCode != null) {
            builder.appendQueryParameter("parkCode", parkCode);
        } else {
            builder.appendQueryParameter("parkCode", p.getParkCode());
        }

        url = builder.build().toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            jsonArray = response.getJSONArray("data");
                            JSONObject parkinfo = jsonArray.getJSONObject(0);
                            String name = parkinfo.getString("fullName");
                            String url = parkinfo.getString("url");
                            String description = parkinfo.getString("description");
                            double lat = parkinfo.getDouble("latitude");
                            double lng = parkinfo.getDouble("longitude");

                            JSONArray jsonImages = parkinfo.getJSONArray("images");
                            List<String> imageURLs = new ArrayList<>();

                            for (int j = 0; j < jsonImages.length(); j++) {
                                JSONObject u = jsonImages.getJSONObject(j);
                                imageURLs.add(u.getString("url"));
                            }

                            String parkCode = parkinfo.getString("parkCode");

                            JSONArray jsonActivities = parkinfo.getJSONArray("activities");
                            List<String> activities = new ArrayList<>();

                            for (int j = 0; j < jsonActivities.length(); j++) {
                                JSONObject a = jsonActivities.getJSONObject(j);
                                activities.add(a.getString("name"));
                            }

                            JSONArray jsonFees = parkinfo.getJSONArray("entranceFees");
                            List<String> fees = new ArrayList<>();

                            for (int j = 0; j < jsonFees.length(); j++) {
                                JSONObject k = jsonFees.getJSONObject(j);
                                fees.add(k.getString("title") + ": $"+ k.getString("cost"));

                            }

                            String states = parkinfo.getString("states");

                            JSONObject contacts = parkinfo.getJSONObject("contacts");
                            JSONArray jsonPhonenumbers = contacts.getJSONArray("phoneNumbers");
                            List<cPhoneNumber> phoneNumbers = new ArrayList<>();

                            for (int j = 0; j < jsonPhonenumbers.length(); j++) {
                                JSONObject pn = jsonPhonenumbers.getJSONObject(j);
                                cPhoneNumber n = new cPhoneNumber(pn.getString("phoneNumber"), pn.getString("type"));
                                phoneNumbers.add(n);
                            }

                            JSONArray jsonEmails = contacts.getJSONArray("emailAddresses");
                            JSONObject e = jsonEmails.getJSONObject(0);
                            String email = e.getString("emailAddress");

                            JSONArray jsonAddresses = parkinfo.getJSONArray("addresses");
                            List<cAddress> addresses = new ArrayList<>();

                            for (int j = 0; j < jsonAddresses.length(); j++) {
                                JSONObject addr = jsonAddresses.getJSONObject(j);
                                cAddress address = new
                                        cAddress(addr.getString("postalCode"), addr.getString("city"),
                                        addr.getString("stateCode"), addr.getString("line1"), addr.getString("line2"),
                                        addr.getString("line3"), addr.getString("type"));
                                addresses.add(address);
                            }

                            cContactInformation contactInfo = new cContactInformation(phoneNumbers, addresses, email);

                            String designation = parkinfo.getString("designation");

                            if (p == null) {
                                p = new cPark();
                            }

                            p.setFullName(name);
                            p.setUrl(url);
                            p.setDescription(description);
                            p.setLat(lat);
                            p.setLng(lng);
                            p.setImages(imageURLs);
                            p.setParkCode(parkCode);
                            p.setActivities(activities);
                            p.setStates(states);
                            p.setContactInformation(contactInfo);
                            p.setDesignation(designation);
                            p.setFees(fees);

                            getPlaceIds();
                            DisplayInformation();

                        } catch (JSONException e) {
                            txtName.setText("GetParkInformation JSON error");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        txtName.setText("GetParkInformation API error");

                    }
                });
        RequestSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void DisplayInformation() {
        txtName.setText(p.getFullName());
        txtDescription.setText(p.getDescription());
        Picasso.get().load(Uri.parse(p.getImages().get(0))).resize(2048, 1600).onlyScaleDown().into(parkBack);
    }

    private void nearbyNotifications() {

            AlertDialog.Builder builder = new AlertDialog.Builder(ParkInformationActivity.this);
            builder.setCancelable(true);
            builder.setTitle("Unvisited Park"+ " " +p.getFullName());
            builder.setMessage("You have not added this park to your visited list. Would you like to?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DatabaseReference ref = mdatabase.child("/Profile/VisitedList").child(fAuth.getCurrentUser().getUid());
                    ref.addValueEventListener(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if (!isAdded){

                                if (snapshot.child("parkCodes").exists()) {
                                String parkCodes = snapshot.child("parkCodes").getValue().toString();
                                List<String> parkCodeList = new ArrayList<String>(Arrays.asList(parkCodes.split(",")));

                                parkCodeList.add(p.getParkCode() + ",");

                                parkCodes = String.join(",", parkCodeList);
                                ref.child("parkCodes").setValue(parkCodes);

                            } else {
                                ref.child("parkCodes").setValue(p.getParkCode() + ",");
                            }

                            isAdded = true;
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        }
                    });
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(ParkInformationActivity.this, "Park Not Saved", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                }
            });
            builder.show();


    }
    private void removeNearby() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ParkInformationActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Remove"+ " " +p.getFullName());
        builder.setMessage("Do you want to remove this park from your visited list?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                DatabaseReference ref = mdatabase.child("/Profile/VisitedList").child(fAuth.getCurrentUser().getUid());
                ref.addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if(!isRemoved){


                            if (snapshot.child("parkCodes").exists()){
                                String arrayCodes = snapshot.child("parkCodes").getValue().toString();
                                List<String> parkCodeList = new ArrayList<String>(Arrays.asList(arrayCodes.split(",")));
                                for(int i = 0; i < parkCodeList.size(); i++)
                                    if (p.getParkCode().equals(parkCodeList.get(i)))
                                    {


                                        parkCodeList.remove(i);
                                        String newCodes = String.join(",", parkCodeList);
                                        newCodes = newCodes + ",";
                                        ref.child("parkCodes").setValue(newCodes);




                                    }
                            }


                            isRemoved = true;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {


                    }
                });
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ParkInformationActivity.this, "Park Not Saved", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void Checkpark(){

        DatabaseReference ref2 = mdatabase.child("/Profile/VisitedList").child(fAuth.getCurrentUser().getUid());
        ref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.child("parkCodes").exists()){
                    String arrayCodes = snapshot.child("parkCodes").getValue().toString();
                    parkcodeArr.addAll(Arrays.asList(arrayCodes.split(",")));
                    for(int i = 0; i < parkcodeArr.size(); i++)
                        if (parkCode != null) {
                            if (parkCode.equals(parkcodeArr.get(i)) && isStarted)
                            {
                                visited.setChecked(true);
                            }
                        } else if (p != null) {
                            if (p.getParkCode().equals(parkcodeArr.get(i)) && isStarted)
                            {
                                visited.setChecked(true);
                            }
                        }
                }
                isStarted = false;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getPlaceIds() {
        DiskBasedCache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());

        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();

        builder = new Uri.Builder();
        builder.scheme("https").authority("developer.nps.gov").appendPath("api").appendPath("v1")
                .appendPath("amenities").appendPath("parksplaces");

        builder.appendQueryParameter("api_key", API_KEY);

        if (parkCode != null) {
            builder.appendQueryParameter("parkCode", parkCode);
        } else {
            builder.appendQueryParameter("parkCode", p.getParkCode());
        }

        builder.appendQueryParameter("id", PARKS_PLACES_ID);

        url = builder.build().toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (!response.getString("total").equals("0")) {
                                jsonArray = response.getJSONArray("data");
                                JSONArray jsonArray1 = jsonArray.getJSONArray(0);
                                JSONObject jsonObject = jsonArray1.getJSONObject(0);
                                JSONArray jsonArray2 = jsonObject.getJSONArray("parks");
                                JSONObject jsonObject1 = jsonArray2.getJSONObject(0);
                                JSONArray jsonArray3 = jsonObject1.getJSONArray("places");

                                for (int i = 0; i < jsonArray3.length(); i++) {
                                    JSONObject place = jsonArray3.getJSONObject(i);
                                    String id = place.getString("id");
                                    placeIds.add(id);
                                }

                                if (placeIds.size() > 0) {
                                    getPlaceInfo();
                                }
                            } else {
                                getVisitorCenters();
                            }

                        } catch (JSONException e) {
                            txtName.setText("getPlaceIds JSON Error");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        txtName.setText("getPlaceIds API Error");

                    }
                });
        RequestSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

    }

    private void getPlaceInfo() {
        DiskBasedCache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());

        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();

        builder = new Uri.Builder();
        builder.scheme("https").authority("developer.nps.gov").appendPath("api").appendPath("v1")
                .appendPath("places");

        builder.appendQueryParameter("api_key", API_KEY);
        builder.appendQueryParameter("limit", "1000");

        if (parkCode != null) {
            builder.appendQueryParameter("parkCode", parkCode);
        } else {
            builder.appendQueryParameter("parkCode", p.getParkCode());
        }

        url = builder.build().toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            jsonArray = response.getJSONArray("data");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject place = jsonArray.getJSONObject(i);
                                String id = place.getString("id");

                                if (!placeIds.contains(id)) {
                                    continue;
                                }

                                String name = place.getString("title");
                                String placeurl = place.getString("url");
                                String desc = place.getString("listingDescription");


                                if (!place.getString("latitude").equals("") || !place.getString("latitude").equals("")) {

                                    double lat = (double) place.getDouble("latitude");
                                    double lng = (double) place.getDouble("longitude");

                                    JSONArray jsonImages = place.getJSONArray("images");
                                    List<String> imageURLs = new ArrayList<>();

                                    for (int j = 0; j < jsonImages.length(); j++) {
                                        JSONObject u = jsonImages.getJSONObject(j);
                                        imageURLs.add(u.getString("url"));
                                    }

                                    List<String> fees = new ArrayList<>();
                                    fees.add("There is no entrance fee. Activities may require a fee. Please see website for more information.");

                                    JSONArray jsonAmenities = place.getJSONArray("amenities");
                                    List<String> amenities = new ArrayList<>();

                                    for (int j = 0; j < jsonAmenities.length(); j++) {
                                        amenities.add(jsonAmenities.get(j).toString());
                                    }

                                    cPlace newPlace = new cPlace(name, placeurl, desc, lat, lng, imageURLs, amenities, fees);
                                    newPlace.setType("Landmark");
                                    places.add(newPlace);

                                    LatLng placeLatLong = new LatLng(newPlace.getLat(), newPlace.getLng());

                                    mMap.addMarker(new MarkerOptions()
                                            .position(placeLatLong)
                                            .title(newPlace.getFullName())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))).setVisible(true);

                                    allPlaces = places;
                                }
                            }

                            getVisitorCenters();

                        } catch (JSONException e) {
                            txtName.setText("getPlaceInfo JSON error");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        txtName.setText("getPlaceInfo API error");
                    }
                });
        RequestSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void getVisitorCenters() {
        DiskBasedCache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());

        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();

        builder = new Uri.Builder();
        builder.scheme("https").authority("developer.nps.gov").appendPath("api").appendPath("v1")
                .appendPath("visitorcenters");

        builder.appendQueryParameter("api_key", API_KEY);

        if (parkCode != null) {
            builder.appendQueryParameter("parkCode", parkCode);
        } else {
            builder.appendQueryParameter("parkCode", p.getParkCode());
        }

        url = builder.build().toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            jsonArray = response.getJSONArray("data");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject place = jsonArray.getJSONObject(i);
                                String name = place.getString("name");
                                String placeurl = place.getString("url");
                                String desc = place.getString("description");

                                if (place.getString("latitude").equals("") || place.getString("latitude").equals("")) {
                                    continue;
                                }

                                double lat = (double) place.getDouble("latitude");
                                double lng = (double) place.getDouble("longitude");

                                JSONArray jsonImages = place.getJSONArray("images");
                                List<String> imageURLs = new ArrayList<>();

                                for (int j = 0; j < jsonImages.length(); j++) {
                                    JSONObject u = jsonImages.getJSONObject(j);
                                    imageURLs.add(u.getString("url"));
                                }

                                List<String> fees = new ArrayList<>();
                                fees.add("There is no entrance fee. Activities may require a fee. Please see website for more information.");

                                JSONArray jsonAmenities = place.getJSONArray("amenities");
                                List<String> amenities = new ArrayList<>();

                                for (int j = 0; j < jsonAmenities.length(); j++) {
                                    amenities.add(jsonAmenities.get(j).toString());
                                }

                                cPlace visitorCenter = new cPlace(name, placeurl, desc, lat, lng, imageURLs, amenities, fees);
                                visitorCenter.setType("Visitor Center");
                                visitorCenters.add(visitorCenter);
                            }

                            for (int i = 0; i < visitorCenters.size(); i++) {
                                LatLng vcLatLong = new LatLng(visitorCenters.get(i).getLat(), visitorCenters.get(i).getLng());

                                mMap.addMarker(new MarkerOptions()
                                        .position(vcLatLong)
                                        .title(visitorCenters.get(i).getFullName())
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))).setVisible(true);
                            }

                            allCenters = visitorCenters;
                            getCamgrounds();

                        } catch (JSONException e) {
                            txtName.setText("getVisitorCenter JSON error ");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        txtName.setText("getVisitorCenter API error");
                        error.printStackTrace();
                    }
                });
        RequestSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void getCamgrounds() {
        DiskBasedCache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());

        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();

        builder = new Uri.Builder();
        builder.scheme("https").authority("developer.nps.gov").appendPath("api").appendPath("v1")
                .appendPath("campgrounds");

        builder.appendQueryParameter("api_key", API_KEY);
        builder.appendQueryParameter("limit", "1000");

        if (parkCode != null) {
            builder.appendQueryParameter("parkCode", parkCode);
        } else {
            builder.appendQueryParameter("parkCode", p.getParkCode());
        }

        url = builder.build().toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            jsonArray = response.getJSONArray("data");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject place = jsonArray.getJSONObject(i);
                                String name = place.getString("name");
                                String placeurl = place.getString("url");
                                String desc = place.getString("description");

                                if (place.getString("latitude").equals("") || place.getString("latitude").equals("")) {
                                    continue;
                                }

                                double lat = (double) place.getDouble("latitude");
                                double lng = (double) place.getDouble("longitude");

                                JSONArray jsonImages = place.getJSONArray("images");
                                List<String> imageURLs = new ArrayList<>();

                                for (int j = 0; j < jsonImages.length(); j++) {
                                    JSONObject u = jsonImages.getJSONObject(j);
                                    imageURLs.add(u.getString("url"));
                                }

                                JSONArray jsonFees = place.getJSONArray("fees");
                                List<String> fees = new ArrayList<>();

                                fees.add("The following fees are for nightly/daily rates, unless stated otherwise.\n");

                                for (int j = 0; j < jsonFees.length(); j++) {
                                    JSONObject f = jsonFees.getJSONObject(j);
                                    fees.add(f.getString("title") + ": $" + f.getString("cost"));
                                }

                                List<String> amenities = new ArrayList<>();
                                amenities.add("Please see website for amenities and seasonal availability.");

                                cPlace campground = new cPlace(name, placeurl, desc, lat, lng, imageURLs, amenities, fees);
                                campground.setType("Campground");
                                campgrounds.add(campground);
                            }

                            for (int i = 0; i < campgrounds.size(); i++) {
                                LatLng vcLatLong = new LatLng(campgrounds.get(i).getLat(), campgrounds.get(i).getLng());

                                mMap.addMarker(new MarkerOptions()
                                        .position(vcLatLong)
                                        .title(campgrounds.get(i).getFullName())
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).setVisible(true);
                            }

                            //allCenters = visitorCenters;

                        } catch (JSONException e) {
                            txtName.setText("getCampgrounds JSON error ");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        txtName.setText("getCampgrounds API error");
                        error.printStackTrace();
                    }
                });
        RequestSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
}
