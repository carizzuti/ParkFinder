package com.fullsail.parkfinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParksVisitedActivity extends AppCompatActivity {

    private ImageView txtBack;
    private FirebaseDatabase database;
    private DatabaseReference parkRef;
    private DatabaseReference visitedRef;
    private FirebaseAuth fAuth;
    private static final String PARK =  "/Profile/VisitedList";
    private static final String visited =  "/Profile/VisitedList";
    private ArrayList<String> parkcodeArr;
    private ArrayList<cPark> parks;
    private Uri.Builder builder;
    private RequestQueue requestQueue;
    private String url;
    private JSONArray jsonArray;
    private RecyclerView recyclerView;
    private String uid;
    private TextView novisit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parks_visited);

        txtBack = findViewById(R.id.back);
        recyclerView = findViewById(R.id.visitedRecyclerView);
        novisit = findViewById(R.id.novisit);
        fAuth = FirebaseAuth.getInstance();
        parkcodeArr = new ArrayList<>();
        parks = new ArrayList<>();

        Intent i = getIntent();
        uid = i.getStringExtra("uid");
        getParkVisited();

        txtBack.setOnClickListener(v -> startActivity(new Intent(ParksVisitedActivity.this, AccountActivity.class)));
    }

    private void getParkVisited() {

        database = FirebaseDatabase.getInstance();
        parkRef = database.getReference(PARK).child(uid);
        parkRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                if (snapshot.child("parkCodes").exists()){
                    String arrayCodes = snapshot.child("parkCodes").getValue().toString();
                    parkcodeArr.addAll(Arrays.asList(arrayCodes.split(",")));

                    getParkInformation();
                } else {
                    if (parkcodeArr.size() > 0) {
                        novisit.setVisibility(View.INVISIBLE);
                    } else {
                        novisit.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getParkInformation() {

        for (int i = 0; i < parkcodeArr.size(); i++) {
            getParkInformation(parkcodeArr.get(i));
        }

    }

    private void getParkInformation(String code){
        DiskBasedCache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());

        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();

        builder = new Uri.Builder();
        builder.scheme("https").authority("developer.nps.gov").appendPath("api").appendPath("v1")
                .appendPath("parks");

        builder.appendQueryParameter("api_key", "qYfJXGgu3oc42jxLtI08WTqcGhfWsWRh0a60EYe8");

        builder.appendQueryParameter("parkCode", code);

        url = builder.build().toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            jsonArray = response.getJSONArray("data");
                            JSONObject parkinfo = jsonArray.getJSONObject(0);
                            String name = parkinfo.getString("fullName");
                            String des = parkinfo.getString("designation");

                            JSONArray jsonImages = parkinfo.getJSONArray("images");
                            List<String> imageURLs = new ArrayList<>();

                            for (int j = 0; j < jsonImages.length(); j++) {
                                JSONObject u = jsonImages.getJSONObject(j);
                                imageURLs.add(u.getString("url"));
                            }

                            cPark p = new cPark();

                            p.setFullName(name);
                            p.setParkCode(code);
                            p.setImages(imageURLs);
                            p.setDesignation(des);

                            parks.add(p);

                            ParksVisitedRVAdapter adapter = new ParksVisitedRVAdapter(parks, ParksVisitedActivity.this, uid);
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(ParksVisitedActivity.this));

                        } catch (JSONException e) {
                            Toast.makeText(ParksVisitedActivity.this,"JSON error", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ParksVisitedActivity.this,"JSON error", Toast.LENGTH_LONG).show();

                    }
                });
        RequestSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
}
