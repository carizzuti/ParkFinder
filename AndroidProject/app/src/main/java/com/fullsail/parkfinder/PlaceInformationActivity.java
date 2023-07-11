package com.fullsail.parkfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class PlaceInformationActivity extends AppCompatActivity {

    private cPlace p;
    private TextView name, desc, fees, amenities, website;
    private ImageView back, placeBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_information);

        p = new cPlace();
        name = findViewById(R.id.placeName);
        desc = findViewById(R.id.placeDescription);
        back = findViewById(R.id.back);
        fees = findViewById(R.id.feesContent);
        amenities = findViewById(R.id.amenitiesContent);
        website = findViewById(R.id.websiteContent);
        placeBackground = findViewById(R.id.placeImageBack);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent i = getIntent();
        p = (cPlace) i.getSerializableExtra("place");

        displayInformation();
    }

    private void displayInformation() {

        Picasso.get().load(Uri.parse(p.getImages().get(0))).resize(2048, 1600).onlyScaleDown().into(placeBackground);

        name.setText(p.getFullName());
        desc.setText("\n");
        desc.append(p.getDescription());
        desc.append("\n");
        website.setText(p.getUrl());
        website.append("\n");

        amenities.setText("");
        for (int i = 0; i < p.getAmenities().size(); i++) {
            amenities.append(p.getAmenities().get(i) + "\n");
        }

        fees.setText("");
        for (int i = 0; i < p.getFees().size(); i++) {
            fees.append(p.getFees().get(i) + "\n");
        }
    }
}