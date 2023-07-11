package com.fullsail.parkfinder;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LandmarkListActivity extends AppCompatActivity {

    private List<cPlace> landmarks;
    private List<cPlace> visitorCenters;
    private List<cPlace> campgrounds;
    private List<cPlace> finalList;
    private TextView progress;
    private ImageView btnBack, headerBackground;
    private Button save;
    private RecyclerView recyclerView;
    private LandmarkListRVAdapter adapter;
    private cPark park;
    private List<String> checkedLandmarks;
    private boolean fromLocator, fromVisited = false;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark_list);

        finalList = new ArrayList<>();
        btnBack = findViewById(R.id.back);
        recyclerView = findViewById(R.id.landmarkListRecyclerView);
        save = findViewById(R.id.btnSave);
        progress = findViewById(R.id.progress);
        headerBackground = findViewById(R.id.landmarksImageBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LandmarkListActivity.this, ParkInformationActivity.class);
                intent.putExtra("park", park);
                intent.putExtra("fromLocator", fromLocator);
                intent.putExtra("fromVisited", fromVisited);

                if (fromVisited) {
                    intent.putExtra("uid", uid);
                }

                startActivity(intent);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();

        Intent i = getIntent();
        landmarks = (List<cPlace>) i.getSerializableExtra("landmarkList");
        visitorCenters = (List<cPlace>) i.getSerializableExtra("visitorCenterList");
        campgrounds = (List<cPlace>) i.getSerializableExtra("campgroundsList");
        checkedLandmarks = (List<String>) i.getStringArrayListExtra("checkedLandmarks");
        park = (cPark) i.getSerializableExtra("park");
        fromLocator = i.getBooleanExtra("fromLocator", false);
        fromVisited = i.getBooleanExtra("fromVisited", false);
        uid = i.getStringExtra("uid");

        if (park != null) {
            if (park.getImages().size() > 1) {
                Picasso.get().load(Uri.parse(park.getImages().get(1))).into(headerBackground);
            } else {
                Picasso.get().load(Uri.parse(park.getImages().get(0))).resize(2048, 1600).onlyScaleDown().into(headerBackground);
            }

        }

        if (fromVisited) {
            save.setVisibility(View.INVISIBLE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.landmarkHeaderLayout);
        }

        filterDuplicates();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(LandmarkListActivity.this, ParkInformationActivity.class);
        intent.putExtra("park", park);
        intent.putExtra("fromLocator", fromLocator);
        intent.putExtra("fromVisited", fromVisited);

        if (fromVisited) {
            intent.putExtra("uid", uid);
        }

        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void filterDuplicates() {

        finalList.clear();
        if (visitorCenters != null) {
            finalList.addAll(visitorCenters);
        }

        if (landmarks != null) {
            for (int i = 0; i < landmarks.size(); i++) {

                boolean duplicate = false;

                for (int j = 0; j < visitorCenters.size(); j++) {
                    if (landmarks.get(i).getFullName().equals(visitorCenters.get(j).getFullName())) {
                        duplicate = true;
                        break;
                    }
                }

                if (!duplicate) {
                    finalList.add(landmarks.get(i));
                }
            }
        }

        if (campgrounds != null) {
            finalList.addAll(campgrounds);
        }

        finalList = finalList.stream()
                .distinct()
                .collect(Collectors.toList());

        Comparator<cPlace> compareByName = new Comparator<cPlace>() {
            @Override
            public int compare(cPlace o1, cPlace o2) {
                return o1.getFullName().compareTo(o2.getFullName());
            }
        };

        Collections.sort(finalList, compareByName);

        adapter = new LandmarkListRVAdapter(LandmarkListActivity.this, finalList, save, progress, park.getParkCode(), checkedLandmarks, fromVisited);
        recyclerView.setAdapter(adapter);
    }
}