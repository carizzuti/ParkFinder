package com.fullsail.parkfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsListActivity extends AppCompatActivity {

    private FirebaseListAdapter<cUsers> adapter;
    private DatabaseReference mbase;
    private ListView friendsList;
    private SearchView editSearch;
    private FirebaseAuth fAuth;
    private ImageView back;
    private TextView nofriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        mbase = FirebaseDatabase.getInstance().getReference();
        fAuth = FirebaseAuth.getInstance();

        back = findViewById(R.id.back);
        friendsList = findViewById(R.id.friendsListview);
        nofriends = findViewById(R.id.nofriends);
        editSearch = findViewById(R.id.friendSearch);
        editSearch.clearFocus();

        friendsList.setClickable(true);
        friendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                cUsers u = (cUsers) friendsList.getItemAtPosition(position);
                Intent i = new Intent(FriendsListActivity.this, OtherUserActivity.class);
                i.putExtra("uid", u.getUid());
                startActivity(i);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FriendsListActivity.this, AccountActivity.class);
                startActivity(i);
            }
        });

        editSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                FirebaseListOptions<cUsers> options =
                        new FirebaseListOptions.Builder<cUsers>()
                                .setLayout(R.layout.friendslist_view)
                                .setQuery(mbase.child("Profile").child("Friends").child(Objects.requireNonNull(fAuth.getCurrentUser()).getUid())
                                        .orderByChild("username").startAt(s).endAt(s+"\uf8ff"), cUsers.class)
                                .build();

                adapter.stopListening();
                adapter = CreateListAdapter(options);
                friendsList.setAdapter(adapter);
                adapter.startListening();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                FirebaseListOptions<cUsers> options =
                        new FirebaseListOptions.Builder<cUsers>()
                                .setLayout(R.layout.friendslist_view)
                                .setQuery(mbase.child("Profile").child("Friends").child(Objects.requireNonNull(fAuth.getCurrentUser()).getUid())
                                        .orderByChild("username").startAt(s).endAt(s+"\uf8ff"), cUsers.class)
                                .build();

                adapter.stopListening();
                adapter = CreateListAdapter(options);
                friendsList.setAdapter(adapter);
                adapter.startListening();
                return false;
            }
        });

        FirebaseListOptions<cUsers> options =
                new FirebaseListOptions.Builder<cUsers>()
                        .setLayout(R.layout.friendslist_view)
                        .setQuery(mbase.child("Profile").child("Friends").child(Objects.requireNonNull(fAuth.getCurrentUser()).getUid()), cUsers.class)
                        .build();

        adapter = CreateListAdapter(options);
        friendsList.setAdapter(adapter);

        mbase.child("Profile").child("Friends").child(Objects.requireNonNull(fAuth.getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    nofriends.setVisibility(View.INVISIBLE);
                    editSearch.setVisibility(View.VISIBLE);
                } else {
                    nofriends.setVisibility(View.VISIBLE);
                    editSearch.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public FirebaseListAdapter<cUsers> CreateListAdapter(FirebaseListOptions<cUsers> options) {

        return new FirebaseListAdapter<cUsers>(options) {
            @Override
            protected void populateView(@NonNull @NotNull View v, @NonNull @NotNull cUsers model, int position) {
                TextView fullName, userName;
                CircleImageView imgURL;
                fullName = v.findViewById(R.id.friendName);
                userName = v.findViewById(R.id.friendUsername);
                imgURL = v.findViewById(R.id.friendImage);

                DatabaseReference ref = mbase.child("Profile").child("UserInfo").child(model.getUid());
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        fullName.setText(Objects.requireNonNull(snapshot.child("fullname").getValue()).toString());
                        userName.setText(Objects.requireNonNull(snapshot.child("username").getValue()).toString());
                        Picasso.get().load(Uri.parse(Objects.requireNonNull(snapshot.child("imgURL").getValue()).toString())).into(imgURL);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        };
    }
}