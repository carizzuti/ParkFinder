package com.fullsail.parkfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchFiltersActivity extends AppCompatActivity {

    private AutoCompleteTextView acParkName, acDesignation;
    private EditText etRadius, etParksToDisplay, etZipCode;
    private Button btnSaveFilters, btnParkNameSearch;
    private Uri.Builder builder;
    private RequestQueue requestQueue;
    private String url;
    private JSONArray jsonArray;
    private cPark p;
    private ArrayList<cPark> parks;
    private ArrayList<String> designations;
    private ArrayAdapter<cPark> parkNameArrayAdapter;
    private ArrayAdapter<String> designationArrayAdapter;
    private boolean isValid = true;
    private TextView zipError, displayNumberError, radiusError;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_filters);

        acParkName = findViewById(R.id.autoCompleteParkNameSearch);
        acDesignation = findViewById(R.id.autoCompleteDesignation);
        etRadius = findViewById(R.id.etRadius);
        etParksToDisplay = findViewById(R.id.etParksToDisplay);
        etZipCode = findViewById(R.id.etZipCode);
        btnSaveFilters = findViewById(R.id.btnSaveFilters);
        btnParkNameSearch = findViewById(R.id.btnNameSearch);
        zipError = findViewById(R.id.zipCodeError);
        displayNumberError = findViewById(R.id.displayNumberError);
        radiusError = findViewById(R.id.radiusError);
        back = findViewById(R.id.back);
        parks = new ArrayList<>();
        designations = new ArrayList<>();

        p = new cPark();

        etRadius.setText("50");
        etParksToDisplay.setText("3");

        btnParkNameSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SearchFiltersActivity.this, ParkLocatorActivity.class);
                i.putExtra("park", p);
                startActivity(i);
            }
        });

        btnSaveFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterResults();
            }
        });

        acParkName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                p = parkNameArrayAdapter.getItem(position);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchFiltersActivity.this, ParkLocatorActivity.class);
                startActivity(intent);
            }
        });

        getParkInformation();
    }

    private void getDesignations() {
        ArrayList<String> holder = new ArrayList<>();

        Collections.sort(designations);

        for (int i = 0; i < designations.size(); i++) {
            if (i == 0) {
                holder.add(designations.get(i));
            } else if (designations.get(i).equals(designations.get(i - 1))) {
                continue;
            } else {
                holder.add(designations.get(i));
            }
        }

        designations = holder;

        designationArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, designations);
        acDesignation.setAdapter(designationArrayAdapter);
    }

    private void getParkNames() {
        parkNameArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, parks);
        acParkName.setAdapter(parkNameArrayAdapter);
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
                                String parkCode = parkinfo.getString("parkCode");
                                double lat = (double) parkinfo.getDouble("latitude");
                                double lng = (double) parkinfo.getDouble("longitude");
                                String des = parkinfo.getString("designation");

                                JSONArray jsonImages = parkinfo.getJSONArray("images");
                                List<String> imageURLs = new ArrayList<>();

                                JSONObject u = jsonImages.getJSONObject(0);
                                imageURLs.add(u.getString("url"));

                                cPark park = new cPark();
                                park.setFullName(name);
                                park.setParkCode(parkCode);
                                park.setLat(lat);
                                park.setLng(lng);
                                park.setDesignation(des);
                                park.setImages(imageURLs);

                                parks.add(park);
                                designations.add(des);
                            }

                            getParkNames();
                            getDesignations();

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "JSON ERROR", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "ERROR RESPONSE", Toast.LENGTH_LONG).show();

                    }
                });

        RequestSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void filterResults() {
        ArrayList<cPark> filteredList = new ArrayList<>();

        if (acDesignation.getText().length() != 0) {
            for (int i = 0; i < parks.size(); i++) {
                if (acDesignation.getText().toString().equals("")) {
                    filteredList = parks;
                    break;
                }

                if (parks.get(i).getDesignation().equals("")) {
                    continue;
                } else if (parks.get(i).getDesignation().equals(acDesignation.getText().toString())) {
                    filteredList.add(parks.get(i));
                }
            }
        } else {
            filteredList = parks;
        }

        if (etZipCode.getText().length() == 5 || etZipCode.getText().length() == 0) {
            if (etZipCode.getText().length() == 5) {
                final Geocoder geocoder = new Geocoder(this);

                try {
                    List<Address> addresses = geocoder.getFromLocationName(etZipCode.getText().toString(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        isValid = true;
                    } else {
                        etZipCode.setBackgroundResource(R.drawable.textbox_error);
                        zipError.setText(R.string.ZipNotFound);
                        zipError.setVisibility(View.VISIBLE);
                        isValid = false;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            etZipCode.setBackgroundResource(R.drawable.textbox_error);
            zipError.setText(R.string.ZipInvalid);
            zipError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        if (isValid) {
            if (Integer.parseInt(etParksToDisplay.getText().toString()) < 1) {
                etParksToDisplay.setBackgroundResource(R.drawable.textbox_error);
                displayNumberError.setVisibility(View.VISIBLE);
                isValid = false;
            } else if (etParksToDisplay.getText().length() == 0) {
                isValid = true;
            } else {
                isValid = true;
            }
        }

        if (isValid) {
            if (Integer.parseInt(etRadius.getText().toString()) < 1) {
                etRadius.setBackgroundResource(R.drawable.textbox_error);
                radiusError.setText(R.string.RadiusTooLow);
                radiusError.setVisibility(View.VISIBLE);
                isValid = false;
            } else if (etZipCode.getText().length() == 5 && Integer.parseInt(etRadius.getText().toString()) < 50) {
                etRadius.setBackgroundResource(R.drawable.textbox_error);
                radiusError.setText(R.string.ZipRadiusError);
                radiusError.setVisibility(View.VISIBLE);
                isValid = false;
            } else if (etRadius.getText().length() == 0) {
                isValid = true;
            } else {
                isValid = true;
            }
        }

        if (isValid) {
            Intent i = new Intent(SearchFiltersActivity.this, ParkLocatorActivity.class);
            i.putExtra("filteredParkList", filteredList);
            i.putExtra("radius", etRadius.getText().toString());
            i.putExtra("displayCount", etParksToDisplay.getText().toString());
            i.putExtra("zipCode", etZipCode.getText().toString());
            startActivity(i);
        }
    }
}