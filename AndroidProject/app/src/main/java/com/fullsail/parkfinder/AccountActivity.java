package com.fullsail.parkfinder;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

public class AccountActivity extends AppCompatActivity {

    private Button   btnEditProfile;
    private ImageView imgProfile, addfriends, mainMenubtn, notifications;
    private TextView  userAcc, bio, fullname, postCount, parksVisited, friendcount,
            bronze, silver, gold, plat, requestCount;
    private LinearLayout visitedLayout, friendLayout;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private DatabaseReference visitedRef;
    private ImageButton addPost;
    private static final String USERS =  "/Profile/UserInfo";
    private static final String PARK =  "/Profile/VisitedList";
    private boolean alreadyExecuted;
    private RecyclerView recyclerView;
    private AccountRVAdapter mAdapter;
    private  int count;
    private ArrayList<String> parkcodeArr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_main);
        addfriends = findViewById(R.id.addFriends);
        userAcc = findViewById(R.id.usernameAcc);
        bio = findViewById(R.id.bio);
        fullname = findViewById(R.id.fullname);
        imgProfile = findViewById(R.id.imageProfile);
        btnEditProfile = findViewById(R.id.editProfile);
        mainMenubtn = findViewById(R.id.mainMenubtn);
        firebaseAuth = FirebaseAuth.getInstance();
        addPost = findViewById(R.id.addPostbtn);
        postCount = findViewById(R.id.postCount);
        parksVisited = findViewById(R.id.parksVisited);
        friendcount = findViewById(R.id.following);
        notifications = findViewById(R.id.notifications);
        visitedLayout = findViewById(R.id.pvLayout);
        friendLayout = findViewById(R.id.friendsLayout);
        bronze = findViewById(R.id.bronzeCount);
        silver = findViewById(R.id.silverCount);
        gold = findViewById(R.id.goldCount);
        plat = findViewById(R.id.platCount);
        requestCount = findViewById(R.id.notifCount);
        count = 0;
        parkcodeArr = new ArrayList<>();
        userRef = FirebaseDatabase.getInstance().getReference("/Profile/UserInfo");


        recyclerView = findViewById(R.id.photoView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.HORIZONTAL));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));



        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, editAccount.class);
                startActivity(intent);

            }
        });

        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, friendRequests.class);
                startActivity(intent);
            }
        });

        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
            }
        });

        mainMenubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, MainMenuActivity.class);
                startActivity(intent);
            }
        });

        visitedLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AccountActivity.this, ParksVisitedActivity.class);
                intent.putExtra("uid", firebaseAuth.getCurrentUser().getUid());
                startActivity(intent);

            }
        });

        addfriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, AddFriendActivity.class);
                startActivity(intent);
            }
        });

        friendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, FriendsListActivity.class);
                startActivity(intent);
            }
        });

        DatabaseReference ref = userRef.child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    try {
                        Picasso.get().load(Objects.requireNonNull(snapshot.child("imgURL").getValue()).toString()).into(imgProfile);

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });



        displayInfo();
        displayparkCount();
        displayPosts();
        displayBadges();
        displayNotifications();
    }

    private void displayNotifications() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Profile").child("UserInfo")
                .child(Objects.requireNonNull(firebaseAuth.getUid())).child("requestsReceived");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString()) > 0) {
                        int requests = Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString());
                        requestCount.setText(String.valueOf(requests));
                        requestCount.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void displayBadges() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Profile").child("VisitedList").child(Objects.requireNonNull(firebaseAuth.getUid()));
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

    private void displayPosts() {

        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference listRef = storageReference.child("/users/").child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).child("/Posts/");
        listRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                count = listResult.getItems().size();
                postCount.setText(Integer.toString(count));
                Collections.reverse(listResult.getItems());
                mAdapter = new AccountRVAdapter(AccountActivity.this, listResult);
                recyclerView.setAdapter(mAdapter);

            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(AccountActivity.this, "Fail", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            if (resultCode == Activity.RESULT_OK){

                Uri contentUri = data.getData();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "post_" + timeStamp +"."+getFileExt(contentUri);
                uploadPost(imageFileName,contentUri);
                displayPosts();
            }
        }
    }

    private String getFileExt(Uri contentUri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }

    private void uploadPost(String imageFileName, Uri imageUri) {

        StorageReference postRef = storageReference.child("/users/").child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).child("/Posts/").child(imageFileName);
        postRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AccountActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                postRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Toast.makeText(AccountActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        displayPosts();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(AccountActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayInfo(){

            database = FirebaseDatabase.getInstance();
            userRef = database.getReference(USERS).child(Objects.requireNonNull(firebaseAuth.getUid()));
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                        if(snapshot.exists())
                        {
                            String Username = Objects.requireNonNull(snapshot.child("username").getValue()).toString();
                            String Fullname = Objects.requireNonNull(snapshot.child("fullname").getValue()).toString();
                            String Bio = Objects.requireNonNull(snapshot.child("bio").getValue()).toString();
                            String fcount = Objects.requireNonNull(snapshot.child("friendcount").getValue()).toString();

                            fullname.setText(Fullname);
                            friendcount.setText(fcount);
                            bio.setText(Bio);
                            userAcc.setText(Username);
                        }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
    }

    private void displayparkCount(){

        database = FirebaseDatabase.getInstance();
        userRef = database.getReference(PARK).child(Objects.requireNonNull(firebaseAuth.getUid()));
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.child("parkCodes").exists()){
                    String arrayCodes = Objects.requireNonNull(snapshot.child("parkCodes").getValue()).toString();
                    parkcodeArr.addAll(Arrays.asList(arrayCodes.split(",")));
                    parksVisited.setText(String.valueOf(parkcodeArr.size()));
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}
