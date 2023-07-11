package com.fullsail.parkfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;

public class postDisplay extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_display);
        Intent intent = getIntent();
        PhotoView postImage = findViewById(R.id.postImage);

        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("Bitmap");
        postImage.setImageBitmap(bitmap);


    }


}