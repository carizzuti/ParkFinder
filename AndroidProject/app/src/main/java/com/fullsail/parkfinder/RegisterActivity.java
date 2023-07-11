package com.fullsail.parkfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private Button BtnRegister, google;
    private TextView txtToLogin;
    private EditText emailTxt, passwordTxt,userTxt;
    private FirebaseAuth mAuth;
    private FirebaseDatabase fData;
    private DatabaseReference rData;
    private static final String USER = "Users";
    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        BtnRegister = findViewById(R.id.RegisterBtn);
        txtToLogin = findViewById(R.id.toLoginBtn);
        emailTxt = findViewById(R.id.emailTxt);
        passwordTxt = findViewById(R.id.passwordTxt);
        google = findViewById(R.id.googleSignIn);


        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });




        mAuth = FirebaseAuth.getInstance();
        requestGoogleSignIn();



        if (mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainMenuActivity.class));
            finish();
        }

        BtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailTxt.getText().toString().trim();
                String password = passwordTxt.getText().toString().trim();



                if(TextUtils.isEmpty(email))
                {
                    emailTxt.setError("Email is Required");
                    return;
                }
                if(TextUtils.isEmpty(password))
                {
                    passwordTxt.setError("Password is Required");
                }

                if(password.length() < 6){
                    passwordTxt.setError("Password must be >= 6 Characters");
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this, "User Created.", Toast.LENGTH_SHORT).show();
                            String userId = mAuth.getCurrentUser().getUid();
                            DatabaseReference currentUser_db = FirebaseDatabase.getInstance().getReference().child("Auth").child(userId);
                            Map<String, String> newPost = new HashMap<String, String>();
                            newPost.put("email", email);
                            newPost.put("password", password);

                            DatabaseReference currentUserDefault_db = FirebaseDatabase.getInstance().getReference().child("/Profile/UserInfo").child(userId);
                            currentUserDefault_db.child("imgURL").setValue("https://firebasestorage.googleapis.com/v0/b/park-finder-853ff.appspot.com/o/default%2Fprofile.jpg?alt=media&token=796b15c6-eab8-4359-bbf7-2b0eeb2f7ac9");




                            currentUser_db.setValue(newPost);

                            startActivity(new Intent(getApplicationContext(),editAccount.class));
                        }else{
                            Toast.makeText(RegisterActivity.this, "Error !." + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        txtToLogin.setOnClickListener(v -> startActivity(new  Intent(RegisterActivity.this, LoginActivity.class)));
    }

    private void requestGoogleSignIn(){
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("950798113750-ecn713d1871f04hqeabubcavpmcbieua.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //authenticating user with firebase using received token id
                firebaseAuthWithGoogle(account.getIdToken());



            } catch (ApiException e) {
                Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {

        //getting user credentials with the help of AuthCredential method and also passing user Token Id.
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        //trying to sign in user using signInWithCredential and passing above credentials of user.
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, navigate user to Profile Activity
                            Intent intent = new Intent(RegisterActivity.this,editAccount.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }




}
