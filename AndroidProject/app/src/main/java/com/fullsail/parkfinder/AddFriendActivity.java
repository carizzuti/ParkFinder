package com.fullsail.parkfinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddFriendActivity extends AppCompatActivity {

    private FirebaseListAdapter<cUsers> adapter;
    private DatabaseReference mbase;
    private ListView friendsList;
    private SearchView editSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friends);
        mbase = FirebaseDatabase.getInstance().getReference();

        friendsList = findViewById(R.id.friendsListview);
        editSearch = findViewById(R.id.friendSearch);

        friendsList.setClickable(true);
        friendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                cUsers u = (cUsers) friendsList.getItemAtPosition(position);
                Intent i = new Intent(AddFriendActivity.this, OtherUserActivity.class);
                i.putExtra("uid", u.getUid());
                startActivity(i);
            }
        });

        editSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                FirebaseListOptions<cUsers> options =
                    new FirebaseListOptions.Builder<cUsers>()
                        .setLayout(R.layout.friendslist_view)
                        .setQuery(mbase.child("Profile").child("UserInfo").orderByChild("username").startAt(s).endAt(s+"\uf8ff"), cUsers.class)
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
                                .setQuery(mbase.child("Profile").child("UserInfo").orderByChild("username").startAt(s).endAt(s+"\uf8ff"), cUsers.class)
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
                        .setQuery(mbase.child("Profile").child("UserInfo").orderByChild("username"), cUsers.class)
                        .build();

        adapter = CreateListAdapter(options);
        friendsList.setAdapter(adapter);


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
                fullName.setText(model.getFullname());
                userName.setText(model.getUsername());
                if (model.getImage() != null)
                {
                    //imgURL.setImageURI(Uri.parse(model.getImage()));
                    Picasso.get().load(Uri.parse(model.getImage())).into(imgURL);

                    //Picasso.get().load(Uri.parse(model.getImage())).fit().centerCrop().into(imgURL);
                    //Glide.with(AddFriendActivity.this).load(Uri.parse(model.getImage())).into(imgURL);
                }

            }

        };
    }
}
