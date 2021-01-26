package com.example.ece1778;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class Profile extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView textViewUsername,textViewBio;
    private ImageView profileImage;
    private String uID;
    private Button buttonSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        uID = mAuth.getCurrentUser().getUid();

        // Initialize Cloud Firestore
        db = FirebaseFirestore.getInstance();

        textViewUsername = (TextView) findViewById(R.id.textViewUsername);
        textViewBio = (TextView) findViewById(R.id.textViewBio);
        profileImage = (ImageView) findViewById(R.id.profileImage);
        buttonSignOut = (Button) findViewById(R.id.buttonSignOut);
        buttonSignOut.setOnClickListener(this);

        if(mAuth.getCurrentUser() == null){
            startActivity(new Intent(Profile.this, MainActivity.class));
        }else{
            showProfile();
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSignOut:
                try {
                    // Sign out then Go to login page
                    mAuth.signOut();
                    startActivity(new Intent(this, MainActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void showProfile() {
        Intent intent = getIntent();
        Bitmap imageBitmap = (Bitmap) intent.getParcelableExtra("imageBitmap");
        if(imageBitmap != null){
            profileImage.setImageBitmap(imageBitmap);
        }
        DocumentReference documentReference = db.collection("users").document(uID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Toast.makeText(Profile.this,"Error while loading", Toast.LENGTH_SHORT);
                    Log.d("Profile","-->"+e.toString());
                    return;
                }

                if(documentSnapshot.exists()){
                    String username = documentSnapshot.getString("username");
                    textViewUsername.setText(username);
                    String bio = documentSnapshot.getString("bio");
                    textViewBio.setText(bio);
                    Log.i("Profile","Show profile successfully"+username+" "+bio +uID);
                }

            }
            });
    }
}