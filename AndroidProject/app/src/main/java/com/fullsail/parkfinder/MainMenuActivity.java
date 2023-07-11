package com.fullsail.parkfinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    private Button btnAcc;
    private Button btnLoc;
    private Button btnWish;
    private Button btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        btnAcc = findViewById(R.id.btnAcc);
        btnLoc = findViewById(R.id.btnLocator);
        btnWish = findViewById(R.id.btnWish);
        btnSettings = findViewById(R.id.btnSettings);

        btnAcc.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, AccountActivity.class)));
        btnLoc.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, ParkLocatorActivity.class)));
        btnWish.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, WishlistActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, SettingsActivity.class)));
    }




}


