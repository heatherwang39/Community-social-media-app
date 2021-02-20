package com.example.ece1778;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.annotation.Nullable;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private TextView textViewUsername,textViewBio;
    private ImageView profileImage;
    private String uID;
    private Button buttonSignOut, buttonPost, buttonGlobal;



    private RecyclerView recyclerViewPostList;
    private ArrayList<Post> postList;
    private PostAdapter postAdapter;
    private GridLayoutManager gridLayoutManager;
    private String currentPhotoPath;

    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        uID = mAuth.getCurrentUser().getUid();

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        // Initialize Storage
        storage = FirebaseStorage.getInstance();

        postList = new ArrayList <Post> ();

        textViewUsername = (TextView) findViewById(R.id.textViewUsername);
        textViewBio = (TextView) findViewById(R.id.textViewBio);
        profileImage = (ImageView) findViewById(R.id.profileImage);

        buttonSignOut = (Button) findViewById(R.id.buttonSignOut);
        buttonSignOut.setOnClickListener(this);

        buttonPost = (Button) findViewById(R.id.buttonPost);
        buttonPost.setOnClickListener(this);

        buttonGlobal = (Button) findViewById(R.id.buttonGlobal);
        buttonGlobal.setOnClickListener(this);

        context = ProfileActivity.this;

        if(mAuth.getCurrentUser() == null){
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        }else{
            showProfile();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSignOut:
                try {
                    // Sign out and then go to login page
                    mAuth.signOut();
                    startActivity(new Intent(this, MainActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buttonPost:
                startActivity(new Intent(ProfileActivity.this, Caption.class));
                break;
            case R.id.buttonGlobal:
                startActivity(new Intent(this, GlobalActivity.class));
        }
    }

    private void showProfile() {
        DocumentReference documentReference = db.collection("users").document(uID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Toast.makeText(ProfileActivity.this,"Error while loading", Toast.LENGTH_SHORT);
                    Log.d("Profile","-->"+e.toString());
                    return;
                }

                if(documentSnapshot.exists()){
                    String username = documentSnapshot.getString("username");
                    textViewUsername.setText(username);
                    String bio = documentSnapshot.getString("bio");
                    textViewBio.setText(bio);
                    String profilePic = documentSnapshot.getString("displayPicPath");
                    Glide.with(ProfileActivity.this).load(profilePic).into(profileImage);
                    Log.i("Profile","Show profile successfully"+username+" "+bio +uID);
                }
            }
        });

        //load the post pics
        recyclerViewPostList = (RecyclerView) findViewById(R.id.recyclerViewPostList);
        gridLayoutManager = new GridLayoutManager(this, 3,GridLayoutManager.VERTICAL,false);
        recyclerViewPostList.setLayoutManager(gridLayoutManager);
        loadPosts();
    }

    private void loadPosts() {
        postList.clear();
        CollectionReference collectionReference = db.collection("photos");
        Log.d("Profile uID:",uID);
        collectionReference
                .whereEqualTo("uID", uID)
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Post post = document.toObject(Post.class);
                                postList.add(post);
                                Log.d("Profile", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d("Profile", "Error getting documents: ", task.getException());
                        }
                        postAdapter = new PostAdapter(context, postList);
                        recyclerViewPostList.setAdapter(postAdapter);
                        recyclerViewPostList.setHasFixedSize(true);
                    }
                });
    }



    public CurrentPost getCtx(){
        return ((CurrentPost) getApplicationContext());
    }

}