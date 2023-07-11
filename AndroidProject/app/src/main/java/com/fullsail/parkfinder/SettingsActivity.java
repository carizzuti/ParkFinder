package com.fullsail.parkfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private ImageView BtnMenu;
    private Button save;
    private EditText email, phoneNumber, feedback;
    private FirebaseAuth firebaseAuth;
    private Button btnToggleDark, signout;



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        BtnMenu = findViewById(R.id.btnMenu);
        save = findViewById(R.id.saveContact);
        feedback = findViewById(R.id.feedback);
        email = findViewById(R.id.emailContact);
        phoneNumber = findViewById(R.id.phoneNumber);
        btnToggleDark = findViewById(R.id.darkMode);
        signout = findViewById(R.id.signoutBTN);
        sharedPreffs();
        firebaseAuth = FirebaseAuth.getInstance();


        BtnMenu.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, MainMenuActivity.class)));



        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadToDatabase();

                Intent intent = new Intent(SettingsActivity.this, MainMenuActivity.class);
                startActivity(intent);

            }


        });

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });




    }
    private void uploadToDatabase() {

        String Email = email.getText().toString().trim();
        String PhoneNumber =phoneNumber.getText().toString().trim();
        String FeedBack = feedback.getText().toString().trim();
        if (firebaseAuth.getCurrentUser() != null)
        {
            String userID = firebaseAuth.getCurrentUser().getUid();
            DatabaseReference currentUser_db = FirebaseDatabase.getInstance().getReference().child("/Contact/UserFeedback").child(userID);
            Map<String, String> newPost = new HashMap<>();
            newPost.put("email", Email);
            newPost.put("phonenumber", PhoneNumber);
            newPost.put("feedback", FeedBack);
            currentUser_db.setValue(newPost);
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
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
            btnToggleDark.setText(
                    "Disable Dark Mode");
        }
        else {
            AppCompatDelegate
                    .setDefaultNightMode(
                            AppCompatDelegate
                                    .MODE_NIGHT_NO);
            btnToggleDark
                    .setText(
                            "Enable Dark Mode");
        }



        btnToggleDark.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View view)
                    {
                        // When user taps the enable/disable
                        // dark mode button
                        if (isDarkModeOn) {

                            // if dark mode is on it
                            // will turn it off
                            AppCompatDelegate
                                    .setDefaultNightMode(
                                            AppCompatDelegate
                                                    .MODE_NIGHT_NO);
                            // it will set isDarkModeOn
                            // boolean to false
                            editor.putBoolean(
                                    "isDarkModeOn", false);
                            editor.apply();

                            // change text of Button
                            btnToggleDark.setText(
                                    "Enable Dark Mode");
                        }
                        else {

                            // if dark mode is off
                            // it will turn it on
                            AppCompatDelegate
                                    .setDefaultNightMode(
                                            AppCompatDelegate
                                                    .MODE_NIGHT_YES);

                            // it will set isDarkModeOn
                            // boolean to true
                            editor.putBoolean(
                                    "isDarkModeOn", true);
                            editor.apply();

                            // change text of Button
                            btnToggleDark.setText(
                                    "Disable Dark Mode");
                        }
                    }
                });
    }


}
