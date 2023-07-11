package com.fullsail.parkfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class editAccount extends AppCompatActivity {
    TextView usernameError;
    RelativeLayout changePic;
    ImageView back;
    CircleImageView editImage;
    StorageReference storageReference;
    FirebaseAuth fAuth;
    DatabaseReference mbase;
    EditText username, fullname, bio;
    Button save;
    Uri imageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        changePic = findViewById(R.id.changePicture);
        editImage = findViewById(R.id.profileImage);
        username = findViewById(R.id.usernameEdit);
        fullname = findViewById(R.id.fullnameEdit);
        bio = findViewById(R.id.bioEdit);
        save = findViewById(R.id.savebtn);
        back = findViewById(R.id.back);
        usernameError = findViewById(R.id.usernameError);

        fAuth = FirebaseAuth.getInstance();

        mbase = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(editAccount.this, AccountActivity.class);
                startActivity(intent);
            }
        });

        changePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
            }
        });

        DatabaseReference ref = mbase.child("Profile").child("UserInfo").child(Objects.requireNonNull(fAuth.getCurrentUser()).getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    try {
                        Picasso.get().load(Objects.requireNonNull(snapshot.child("imgURL").getValue()).toString()).into(editImage);

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    try {
                        username.setText(Objects.requireNonNull(snapshot.child("username").getValue()).toString());
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    try {
                        fullname.setText(Objects.requireNonNull(snapshot.child("fullname").getValue()).toString());
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    try {
                        bio.setText(Objects.requireNonNull(snapshot.child("bio").getValue()).toString());
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {




                if (username.getText().length() == 0) {
                    username.setBackgroundResource(R.drawable.textbox_error);
                    Toast.makeText(editAccount.this, "Required Field", Toast.LENGTH_SHORT).show();


                }

                if (fullname.getText().length() == 0) {
                    fullname.setBackgroundResource(R.drawable.textbox_error);
                    Toast.makeText(editAccount.this, "Required Field", Toast.LENGTH_SHORT).show();


                }
                if (bio.getText().length() == 0) {
                    bio.setBackgroundResource(R.drawable.textbox_error);
                    Toast.makeText(editAccount.this, "Required Field", Toast.LENGTH_SHORT).show();


                }else {
                    uploadToDatabase();

                    StorageReference profileRef = storageReference.child("/users/").child(fAuth.getCurrentUser().getUid()).child("/profile.png/");
                    profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(editImage);
                        }
                    });


                    Intent intent = new Intent(editAccount.this, MainMenuActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            if (resultCode == Activity.RESULT_OK){
                assert data != null;
                imageURI = data.getData();
                uploadImagetoFirebase(imageURI);
                //uploadimageDatabase(imageURI);
            }
        }
    }

    private void uploadToDatabase() {

        String Username = username.getText().toString().trim();
        String Fullname = fullname.getText().toString().trim();
        String Bio = bio.getText().toString().trim();
        String Uid = fAuth.getCurrentUser().getUid();
        if (fAuth.getCurrentUser() != null)
        {
            String userID = fAuth.getCurrentUser().getUid();
            DatabaseReference currentUser_db = FirebaseDatabase.getInstance().getReference().child("/Profile/UserInfo").child(userID);

            currentUser_db.child("username").setValue(Username);
            currentUser_db.child("fullname").setValue(Fullname);
            currentUser_db.child("bio").setValue(Bio);
            currentUser_db.child("uid").setValue(Uid);
            currentUser_db.child("friendcount").setValue(0);

            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadImagetoFirebase(Uri imageUri) {
        /*StorageReference fileRef = storageReference.child("/users/"+fAuth.getCurrentUser().getUid()+ "profile.jpg");*/
        StorageReference fileRef = storageReference.child("/users/").child(fAuth.getCurrentUser().getUid()).child("/profile.png/");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(editAccount.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        String userID = fAuth.getCurrentUser().getUid();
                        DatabaseReference currentUser_db = FirebaseDatabase.getInstance().getReference().child("/Profile/UserInfo/").child(userID);
                        currentUser_db.child("imgURL").setValue(String.valueOf(uri));
                        Picasso.get().load(uri).into(editImage);

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(editAccount.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
