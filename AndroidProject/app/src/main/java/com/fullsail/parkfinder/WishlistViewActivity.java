package com.fullsail.parkfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WishlistViewActivity extends AppCompatActivity {

    cWishlistItem w;
    TextView parkname, states, todoList, notes, parkinfoLink;
    ImageView back;
    Button btnEdit, btnDelete;
    FirebaseDatabase database;
    DatabaseReference deleteRef;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wishlist_item_view);

        w = new cWishlistItem();
        parkname = findViewById(R.id.parkName);
        states = findViewById(R.id.states);
        todoList = findViewById(R.id.todoList);
        notes = findViewById(R.id.additionalNotes);
        btnDelete = findViewById(R.id.delete);
        btnEdit = findViewById(R.id.edit);
        parkinfoLink = findViewById(R.id.parkInfoPageLink);
        back = findViewById(R.id.back);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent i = getIntent();
        w = (cWishlistItem) i.getSerializableExtra("item");

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WishlistViewActivity.this, WishlistAddActivity.class);
                intent.putExtra("item", w);
                intent.putExtra("code", w.getParkcode());
                startActivity(intent);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteItem();
                startActivity(new Intent(WishlistViewActivity.this, WishlistActivity.class));
            }
        });

        parkinfoLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WishlistViewActivity.this, ParkInformationActivity.class);
                intent.putExtra("code", w.getParkcode());
                startActivity(intent);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        DisplayInfo();
    }

    private void DeleteItem() {
        database = FirebaseDatabase.getInstance();
        deleteRef = database.getReference("/Wishlist/").child(currentUser.getUid()).child(w.getKey());
        deleteRef.setValue(null);
    }

    private void DisplayInfo() {
        parkname.setText(w.getParkName());
        states.setText(w.getStates());
        todoList.setText(w.getTodoList());
        notes.setText(w.getNotes());
    }
}