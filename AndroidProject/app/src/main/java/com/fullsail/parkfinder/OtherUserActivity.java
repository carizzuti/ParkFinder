package com.fullsail.parkfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class OtherUserActivity extends AppCompatActivity {

    String uid;
    DatabaseReference mbase, FriendsRef, FriendsRequestRef, countRef;
    TextView username, userBio, fullName, postCount, parksVisited, displayCount,
            bronze, silver, gold, plat;
    CircleImageView imgURL;
    ImageView mainMenubtn, backbtn;
    Button addFriendButton;
    boolean isRequested, isRemoved, isSent, isReceived = false;
    private StorageReference storageReference;
    private RecyclerView recyclerView;
    private AccountRVAdapter mAdapter;
    private static final String PARK =  "/Profile/VisitedList";
    private ArrayList<String> parkcodeArr;
    private  int count, friendcount;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, CURRENT_STATE, saveCurrentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user);

        mbase = FirebaseDatabase.getInstance().getReference();
        username = findViewById(R.id.usernameAcc);
        userBio = findViewById(R.id.bio);
        fullName = findViewById(R.id.fullname);
        imgURL = findViewById(R.id.imageProfile);
        postCount = findViewById(R.id.postCount);
        storageReference = FirebaseStorage.getInstance().getReference();
        recyclerView = findViewById(R.id.photoView);
        mainMenubtn = findViewById(R.id.mainMenubtn);
        addFriendButton = findViewById(R.id.addFriendBTN);
        parksVisited = findViewById(R.id.parksVisited);
        displayCount = findViewById(R.id.friendCount);
        backbtn = findViewById(R.id.backbtn);
        bronze = findViewById(R.id.bronzeCount);
        silver = findViewById(R.id.silverCount);
        gold = findViewById(R.id.goldCount);
        plat = findViewById(R.id.platCount);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        count = 0;
        parkcodeArr = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();

        FriendsRequestRef = FirebaseDatabase.getInstance().getReference().child("/Profile/FriendRequests/");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("/Profile/Friends/");
        countRef = FirebaseDatabase.getInstance().getReference().child("/Profile/UserInfo/");


        CURRENT_STATE = "not_friends";

        Intent i = getIntent();
        uid = i.getStringExtra("uid");

        DatabaseReference ref = mbase.child("Profile").child("UserInfo").child(uid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String uName = Objects.requireNonNull(snapshot.child("username").getValue()).toString();
                    String bio = Objects.requireNonNull(snapshot.child("bio").getValue()).toString();
                    String fName = Objects.requireNonNull(snapshot.child("fullname").getValue()).toString();
                    String pImage = Objects.requireNonNull(snapshot.child("imgURL").getValue().toString());
                    String fcount = Objects.requireNonNull(snapshot.child("friendcount").getValue().toString());

                    username.setText(uName);
                    userBio.setText(bio);
                    fullName.setText(fName);
                    displayCount.setText(fcount);
                    Picasso.get().load(Uri.parse(pImage)).into(imgURL);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        mainMenubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherUserActivity.this, MainMenuActivity.class);
                startActivity(intent);
            }
        });

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherUserActivity.this, AccountActivity.class);
                startActivity(intent);
            }
        });

        parksVisited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(OtherUserActivity.this, ParksVisitedActivity.class);
                intent.putExtra("uid", uid);
                startActivity(intent);

            }
        });

        FriendsRequestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.child(senderUserId).child("sent").child(uid).exists()
                        || snapshot.child(uid).child("sent").child(senderUserId).exists()
                        || snapshot.child(uid).child("received").child(senderUserId).exists()
                        || snapshot.child(senderUserId).child("received").child(uid).exists()) {
                    addFriendButton.setText("Pending Request");
                    addFriendButton.setEnabled(false);
                }

                // Cancel request?
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        FriendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.child(senderUserId).child(uid).exists()) {
                    addFriendButton.setText("Remove Friend");
                    CURRENT_STATE = "friends";
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        displayPosts();
        displayparkCount();
        displayBadges();
        //DisplayRecieverFriendCount();


        if (!senderUserId.equals(uid)) {
            addFriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addFriendButton.setEnabled(false);

                    if (CURRENT_STATE.equals("not_friends")) {
                        SendFriendRequestToaPerson();
                    }

                    if (CURRENT_STATE.equals("friends")) {
                        UnFriendAnExistingFriend();
                    }

                }
            });
        }


    }

    private void SendFriendRequestToaPerson()
    {

        DatabaseReference infoRef = mbase.child("Profile").child("UserInfo");
        infoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (!isRequested) {
                    // Sender
                    FriendsRequestRef.child(senderUserId).child("sent").child(uid).child("fullname")
                            .setValue(snapshot.child(uid).child("fullname").getValue());
                    FriendsRequestRef.child(senderUserId).child("sent").child(uid).child("imgURL")
                            .setValue(snapshot.child(uid).child("imgURL").getValue());
                    FriendsRequestRef.child(senderUserId).child("sent").child(uid).child("username")
                            .setValue(snapshot.child(uid).child("username").getValue());
                    FriendsRequestRef.child(senderUserId).child("sent").child(uid).child("uid")
                            .setValue(uid);

                    countRef.child(senderUserId).child("requestsSent").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if (!isSent) {
                                if (snapshot.exists()) {
                                    int requests = Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString());
                                    requests += 1;
                                    countRef.child(senderUserId).child("requestsSent").setValue(requests);
                                } else {
                                    countRef.child(senderUserId).child("requestsSent").setValue(1);
                                }

                                isSent = true;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });

                    // Receiver
                    FriendsRequestRef.child(uid).child("received").child(senderUserId).child("fullname")
                            .setValue(snapshot.child(senderUserId).child("fullname").getValue());
                    FriendsRequestRef.child(uid).child("received").child(senderUserId).child("imgURL")
                            .setValue(snapshot.child(senderUserId).child("imgURL").getValue());
                    FriendsRequestRef.child(uid).child("received").child(senderUserId).child("username")
                            .setValue(snapshot.child(senderUserId).child("username").getValue());
                    FriendsRequestRef.child(uid).child("received").child(senderUserId).child("uid")
                            .setValue(senderUserId);

                    countRef.child(uid).child("requestsReceived").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if (!isReceived) {
                                if (snapshot.exists()) {
                                    int requests = Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString());
                                    requests += 1;
                                    countRef.child(uid).child("requestsReceived").setValue(requests);
                                } else {
                                    countRef.child(uid).child("requestsReceived").setValue(1);
                                }

                                isReceived = true;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });

                    addFriendButton.setText("Pending");

                    isRequested = true;
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void UnFriendAnExistingFriend()
    {
        FriendsRef.child(senderUserId).child(uid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            FriendsRef.child(uid).child(senderUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                addFriendButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                addFriendButton.setText("Add Friend");

                                                countRef.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                                                        if (!isRemoved) {
                                                            if (snapshot.child(senderUserId).child("friendcount").exists()) {
                                                                int newcount = Integer.parseInt(snapshot.child(senderUserId).child("friendcount").getValue().toString());
                                                                countRef.child(senderUserId).child("friendcount").setValue(newcount - 1 );

                                                            }
                                                            if (snapshot.child(uid).child("friendcount").exists()) {
                                                                int newreccount = Integer.parseInt(snapshot.child(uid).child("friendcount").getValue().toString());
                                                                countRef.child(uid).child("friendcount").setValue(newreccount - 1);
                                                            }

                                                            isRemoved = true;
                                                        }


                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                                    }


                                                });


                                            }
                                        }
                                    });
                        }
                    }
                });
    }





    private void displayPosts() {

        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference listRef = storageReference.child("/users/").child(uid).child("/Posts/");
        listRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {

                postCount.setText(Integer.toString(listResult.getItems().size()));
                Collections.reverse(listResult.getItems());
                mAdapter = new AccountRVAdapter(OtherUserActivity.this, listResult);
                recyclerView.setAdapter(mAdapter);

            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(OtherUserActivity.this, "Fail", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayparkCount(){


        DatabaseReference ref = mbase.child(PARK).child(uid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.child("parkCodes").exists()){
                    String arrayCodes = snapshot.child("parkCodes").getValue().toString();
                    parkcodeArr.addAll(Arrays.asList(arrayCodes.split(",")));
                    for(int i = 0; i < parkcodeArr.size(); i++)
                    {
                        count++;
                    }
                    String Count = String.valueOf(count);

                    parksVisited.setText(Count);

                }


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void displayBadges() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Profile").child("VisitedList").child(uid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (!snapshot.child("bronze").exists()) {
                        bronze.setText("0");
                    } else {
                        bronze.setText(Objects.requireNonNull(snapshot.child("bronze").getValue()).toString());
                    }

                    if (!snapshot.child("silver").exists()) {
                        silver.setText("0");
                    } else {
                        silver.setText(Objects.requireNonNull(snapshot.child("silver").getValue()).toString());
                    }

                    if (!snapshot.child("gold").exists()) {
                        gold.setText("0");
                    } else {
                        gold.setText(Objects.requireNonNull(snapshot.child("gold").getValue()).toString());
                    }

                    if (!snapshot.child("plat").exists()) {
                        plat.setText("0");
                    } else {
                        plat.setText(Objects.requireNonNull(snapshot.child("plat").getValue()).toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


    private void DisplayRecieverFriendCount()
    {
        FriendsRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Iterable<DataSnapshot> friendChildren = snapshot.getChildren();
                for(DataSnapshot friendsnap: friendChildren){
                    countRef.child("friendcount").setValue(ServerValue.increment(1));
                    countRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            int newcount = (int) snapshot.child("friendcount").getValue();
                            displayCount.setText(newcount);
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

}