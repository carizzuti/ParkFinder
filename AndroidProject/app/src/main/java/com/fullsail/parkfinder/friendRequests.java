package com.fullsail.parkfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class friendRequests extends AppCompatActivity {

    private FirebaseListAdapter<cUsers> adapter;
    private DatabaseReference mbase;
    private ListView friendsList;
    private FirebaseAuth fAuth;
    private ImageView back;
    String saveCurrentDate, currentUserId;
    boolean isAdded, isSent, isReceived = false;
    DatabaseReference FriendsRef, FriendsRequestRef, countRef;
    private TextView norequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        mbase = FirebaseDatabase.getInstance().getReference();
        fAuth = FirebaseAuth.getInstance();

        currentUserId = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("/Profile/Friends/");
        FriendsRequestRef = FirebaseDatabase.getInstance().getReference().child("/Profile/FriendRequests/");
        countRef = FirebaseDatabase.getInstance().getReference().child("/Profile/UserInfo/");

        back = findViewById(R.id.back);
        friendsList = findViewById(R.id.requestsList);
        norequests = findViewById(R.id.norequests);

        friendsList.setClickable(true);
        friendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                cUsers u = (cUsers) friendsList.getItemAtPosition(position);
                Intent i = new Intent(friendRequests.this, OtherUserActivity.class);
                i.putExtra("uid", u.getUid());
                startActivity(i);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(friendRequests.this, AccountActivity.class);
                startActivity(i);
            }
        });

        FirebaseListOptions<cUsers> options =
                new FirebaseListOptions.Builder<cUsers>()
                        .setLayout(R.layout.friend_request_item)
                        .setQuery(mbase.child("Profile").child("FriendRequests").child(currentUserId).child("received"), cUsers.class)
                        .build();

        adapter = CreateListAdapter(options);
        friendsList.setAdapter(adapter);

        countRef.child(currentUserId).child("requestsReceived").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString()) > 0) {
                        norequests.setVisibility(View.INVISIBLE);
                    } else {
                        norequests.setVisibility(View.VISIBLE);
                    }
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
                Button accept, decline;
                RelativeLayout textLayout;

                fullName = v.findViewById(R.id.fullname);
                userName = v.findViewById(R.id.username);
                imgURL = v.findViewById(R.id.friendimg);
                accept = v.findViewById(R.id.accept);
                decline = v.findViewById(R.id.decline);
                textLayout = v.findViewById(R.id.textLayout);

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

                textLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(friendRequests.this, OtherUserActivity.class);
                        intent.putExtra("uid", model.getUid());
                        startActivity(intent);
                    }
                });

                accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calFordDate = Calendar.getInstance();
                        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                        saveCurrentDate = currentDate.format(calFordDate.getTime());

                        countRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (!isAdded) {
                                    if (snapshot.child(currentUserId).child("friendcount").exists()) {
                                        int newcount = Integer.parseInt(Objects.requireNonNull(snapshot.child(currentUserId).child("friendcount").getValue()).toString());
                                        countRef.child(currentUserId).child("friendcount").setValue(newcount + 1);
                                    }
                                    if (snapshot.child(model.getUid()).child("friendcount").exists()) {
                                        int newreccount = Integer.parseInt(Objects.requireNonNull(snapshot.child(model.getUid()).child("friendcount").getValue()).toString());
                                        countRef.child(model.getUid()).child("friendcount").setValue(newreccount + 1);
                                    }

                                    FriendsRef.child(model.getUid()).child(currentUserId).child("fullname")
                                            .setValue(snapshot.child(currentUserId).child("fullname").getValue());
                                    FriendsRef.child(model.getUid()).child(currentUserId).child("username")
                                            .setValue(snapshot.child(currentUserId).child("username").getValue());
                                    FriendsRef.child(model.getUid()).child(currentUserId).child("uid")
                                            .setValue(currentUserId);
                                    FriendsRef.child(model.getUid()).child(currentUserId).child("imgURL")
                                            .setValue(snapshot.child(currentUserId).child("imgURL").getValue());
                                    FriendsRef.child(model.getUid()).child(currentUserId).child("date")
                                            .setValue(saveCurrentDate);

                                    countRef.child(model.getUid()).child("requestsSent").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                            if (!isSent) {
                                                if (snapshot.exists()) {
                                                    int sentCount = Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString());

                                                    if (sentCount > 0) {
                                                        sentCount -= 1;
                                                    }

                                                    countRef.child(model.getUid()).child("requestsSent").setValue(sentCount);
                                                }
                                            }

                                            isSent = true;
                                        }

                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                        }
                                    });

                                    FriendsRef.child(currentUserId).child(model.getUid()).child("fullname")
                                            .setValue(snapshot.child(model.getUid()).child("fullname").getValue());
                                    FriendsRef.child(currentUserId).child(model.getUid()).child("username")
                                            .setValue(snapshot.child(model.getUid()).child("username").getValue());
                                    FriendsRef.child(currentUserId).child(model.getUid()).child("uid")
                                            .setValue(model.getUid());
                                    FriendsRef.child(currentUserId).child(model.getUid()).child("imgURL")
                                            .setValue(snapshot.child(model.getUid()).child("imgURL").getValue());
                                    FriendsRef.child(currentUserId).child(model.getUid()).child("date")
                                            .setValue(saveCurrentDate);

                                    countRef.child(currentUserId).child("requestsReceived").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                            if (!isReceived) {
                                                if (snapshot.exists()) {
                                                    int receivedCount = Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString());

                                                    if (receivedCount > 0) {
                                                        receivedCount -= 1;
                                                    }

                                                    countRef.child(currentUserId).child("requestsReceived").setValue(receivedCount);
                                                }

                                                isReceived = true;
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                        }
                                    });

                                    FriendsRequestRef.child(currentUserId).child("received").child(model.getUid()).removeValue();
                                    FriendsRequestRef.child(model.getUid()).child("sent").child(currentUserId).removeValue();



                                    isAdded = true;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });

                    }
                });

                decline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FriendsRequestRef.child(currentUserId).child("received").child(model.getUid()).removeValue();
                        FriendsRequestRef.child(model.getUid()).child("sent").child(currentUserId).removeValue();

                        countRef.child(model.getUid()).child("requestsSent").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (!isSent) {
                                    if (snapshot.exists()) {
                                        int sentCount = Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString());

                                        if (sentCount > 0) {
                                            sentCount -= 1;
                                        }

                                        countRef.child(model.getUid()).child("requestsSent").setValue(sentCount);
                                    }
                                }

                                isSent = true;
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });

                        countRef.child(currentUserId).child("requestsReceived").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (!isReceived) {
                                    if (snapshot.exists()) {
                                        int receivedCount = Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString());

                                        if (receivedCount > 0) {
                                            receivedCount -= 1;
                                        }

                                        countRef.child(currentUserId).child("requestsReceived").setValue(receivedCount);
                                    }

                                    isReceived = true;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                    }
                });
            }
        };
    }
}