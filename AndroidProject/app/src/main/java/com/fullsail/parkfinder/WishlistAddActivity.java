package com.fullsail.parkfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WishlistAddActivity extends AppCompatActivity {

    private EditText etStates, etToDoList, etNotes;
    private AutoCompleteTextView etParkName;
    private Button btnSave;
    private DatabaseReference mDatabase;
    private FirebaseUser currentFirebaseUser;
    private cWishlistItem w;
    private DatabaseReference keyref;
    private Uri.Builder builder;
    private RequestQueue requestQueue;
    private String url, imageURL;
    private JSONArray jsonArray;
    private List<cPark> parks;
    private ArrayAdapter<cPark> arrayAdapter;
    private cPark p;
    private String parkCode;
    private TextView parkinfoLink, title;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wishlist_item_add);

        etParkName = findViewById(R.id.autoCompleteParkName);
        etStates = findViewById(R.id.states);
        etToDoList = findViewById(R.id.todoList);
        etNotes = findViewById(R.id.notes);
        btnSave = findViewById(R.id.save);
        parkinfoLink = findViewById(R.id.parkInfoPageLink);
        title = findViewById(R.id.wishlistAddEditTitle);
        back = findViewById(R.id.back);
        parks = new ArrayList<>();
        p = new cPark();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        keyref = mDatabase.child("Wishlist").child(currentFirebaseUser.getUid()).child("wlKey");

        w = new cWishlistItem();

        Intent i = getIntent();
        w = (cWishlistItem) i.getSerializableExtra("item");

        if (w != null) {
            etParkName.setText(w.getParkName());
            etStates.setText(w.getStates());
            etToDoList.setText(w.getTodoList());
            etNotes.setText(w.getNotes());
            parkCode = w.getParkcode();
            imageURL = w.getImageURL();
        }

        if (w != null) {
            title.setText("Edit Item");
        } else {
            title.setText("Add Item");
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (w != null) {
                    EditExistingItem();
                } else {
                    AddNewItem();
                }
            }
        });

        etParkName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                p = arrayAdapter.getItem(position);

                etStates.setText("");

                for (int i = 0; i < p.getStates().size(); i++) {
                    if (i < p.getStates().size() - 1) {
                        etStates.append(p.getStates().get(i) + ", ");
                    } else {
                        etStates.append(p.getStates().get(i));
                    }
                }
                parkCode = p.getParkCode();
            }
        });

        parkinfoLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (p != null) {
                    Intent i = new Intent(WishlistAddActivity.this, ParkInformationActivity.class);
                    i.putExtra("park", p);
                    i.putExtra("code", parkCode);
                    startActivity(i);
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getParkInformation();
    }

    private void AddNewItem() {
        DatabaseReference keyref = mDatabase.child("Wishlist").child(currentFirebaseUser.getUid()).child("wlKey");
        keyref.setValue("key");
        String key = keyref.push().getKey();

        keyref.setValue(null);

        WriteToDatabase(key, true);

        startActivity(new Intent(WishlistAddActivity.this, WishlistActivity.class));
    }

    private void EditExistingItem() {
        WriteToDatabase(w.getKey(), false);
    }

    private void WriteToDatabase(String k, boolean add) {

        getParkImage(k);

        if (add) {
            keyref.removeValue();
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
                                String parkCode = parkinfo.getString("parkCode");
                                String states = parkinfo.getString("states");

                                cPark park = new cPark();
                                park.setFullName(name);
                                park.setParkCode(parkCode);
                                park.setStates(states);

                                parks.add(park);
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

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, parks);
        etParkName.setAdapter(arrayAdapter);

        RequestSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void getParkImage(String k) {
        DiskBasedCache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());

        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();

        builder = new Uri.Builder();
        builder.scheme("https").authority("developer.nps.gov").appendPath("api").appendPath("v1")
                .appendPath("parks");

        builder.appendQueryParameter("api_key", "qYfJXGgu3oc42jxLtI08WTqcGhfWsWRh0a60EYe8");
        builder.appendQueryParameter("parkCode", parkCode);

        url = builder.build().toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            jsonArray = response.getJSONArray("data");
                            JSONObject parkinfo = jsonArray.getJSONObject(0);

                            JSONArray jsonImages = parkinfo.getJSONArray("images");

                            JSONObject u = jsonImages.getJSONObject(0);
                            imageURL = u.getString("url");

                            mDatabase.child("Wishlist").child(currentFirebaseUser.getUid()).child(k).child("ParkImage").setValue(imageURL);
                            mDatabase.child("Wishlist").child(currentFirebaseUser.getUid()).child(k).child("ParkName").setValue(etParkName.getText().toString());
                            mDatabase.child("Wishlist").child(currentFirebaseUser.getUid()).child(k).child("States").setValue(etStates.getText().toString());
                            mDatabase.child("Wishlist").child(currentFirebaseUser.getUid()).child(k).child("ToDoList").setValue(etToDoList.getText().toString());
                            mDatabase.child("Wishlist").child(currentFirebaseUser.getUid()).child(k).child("Notes").setValue(etNotes.getText().toString());
                            mDatabase.child("Wishlist").child(currentFirebaseUser.getUid()).child(k).child("ParkCode").setValue(parkCode);

                            startActivity(new Intent(WishlistAddActivity.this, WishlistActivity.class));

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
}