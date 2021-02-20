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

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private RecyclerView recyclerViewPostList;
    private ArrayList<Post> postList;
    private PostAdapter postAdapter;
    private GridLayoutManager gridLayoutManager;
    private String currentPhotoPath;

    private Context context;
    private Uri postURI;

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
                try {
                    makePost();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    private void makePost() {
        Log.d("Profile","makePost button is clicked!");
        Intent makePostIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (makePostIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(ProfileActivity.this, "Error when creating the Post File",
                        Toast.LENGTH_LONG).show();
            }
            if (photoFile != null) {
                postURI = FileProvider.getUriForFile(this,"com.example.android.fileprovider",photoFile);
                getCtx().setPostUri(postURI);
                makePostIntent.putExtra(MediaStore.EXTRA_OUTPUT, postURI);
                startActivityForResult(makePostIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Log.i("Profile", "onActivityResult: Make Post Image Capture RESULT OK");
            Bitmap postBitmap = null;
            try {
                postBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), postURI);
                getCtx().setPostBitmap(postBitmap);
                Intent intent = new Intent(this, Caption.class);
                startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
        }}else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_CANCELED){
            Log.i("Profile", "onActivityResult: Make Post Image Capture RESULT CANCELLED");
        }else{
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        getCtx().setCurrentPhotoPath(currentPhotoPath);
        return image;
    }

    public CurrentPost getCtx(){
        return ((CurrentPost) getApplicationContext());
    }

}