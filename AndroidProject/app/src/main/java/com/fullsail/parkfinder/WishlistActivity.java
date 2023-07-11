package com.fullsail.parkfinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WishlistActivity extends AppCompatActivity {

    private ImageView btnMenu, btnAdd;
    private FirebaseUser currentFirebaseUser;
    private DatabaseReference mDatabase;
    private String Uid;
    private List<cWishlistItem> wishlistItems;
    private RecyclerView recyclerView;
    private WishlistRVAdapter rvAdapter;
    private FirebaseDatabase database;
    private DatabaseReference wishlistRef;
    private boolean isAdded = false;
    private TextView nowishlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wishlist);

        btnMenu = findViewById(R.id.btnMenu);
        btnAdd = findViewById(R.id.add);
        nowishlist = findViewById(R.id.nowishlist);

        recyclerView = findViewById(R.id.wlRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        wishlistItems = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentFirebaseUser != null) {
            Uid = currentFirebaseUser.getUid();
        }

        btnMenu.setOnClickListener(v -> startActivity(new Intent(WishlistActivity.this, MainMenuActivity.class)));

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WishlistActivity.this, WishlistAddActivity.class));
            }
        });

        displayInfo();

        if (wishlistItems.size() > 0) {
            nowishlist.setVisibility(View.INVISIBLE);
        } else {
            nowishlist.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        displayInfo();
    }

    private void displayInfo(){

        database = FirebaseDatabase.getInstance();
        wishlistRef = database.getReference("/Wishlist/").child(currentFirebaseUser.getUid());
        wishlistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if(!isAdded){
                        Iterable<DataSnapshot> userIdChildren = snapshot.getChildren();
                        for(DataSnapshot wishlistSnap: userIdChildren){

                            String parkname = Objects.requireNonNull(wishlistSnap.child("ParkName").getValue()).toString();
                            String states = Objects.requireNonNull(wishlistSnap.child("States").getValue()).toString();
                            String toDoList = Objects.requireNonNull(wishlistSnap.child("ToDoList").getValue()).toString();
                            String notes = Objects.requireNonNull(wishlistSnap.child("Notes").getValue()).toString();
                            String key = wishlistSnap.getKey();
                            String image = Objects.requireNonNull(wishlistSnap.child("ParkImage").getValue()).toString();
                            String code = Objects.requireNonNull(wishlistSnap.child("ParkCode").getValue()).toString();

                            cWishlistItem wishlist = new  cWishlistItem();
                            wishlist.setParkName(parkname);
                            wishlist.setStates(states);
                            wishlist.setTodoList(toDoList);
                            wishlist.setNotes(notes);
                            wishlist.setKey(key);
                            wishlist.setImageURL(image);
                            wishlist.setParkcode(code);
                            wishlistItems.add(wishlist);
                        }

                        rvAdapter = new WishlistRVAdapter(WishlistActivity.this, wishlistItems);
                        recyclerView.setAdapter(rvAdapter);
                        isAdded = true;
                    }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

}
