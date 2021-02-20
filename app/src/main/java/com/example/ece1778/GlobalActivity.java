package com.example.ece1778;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class GlobalActivity extends AppCompatActivity {
    
    private static final String TAG = "Global";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    
    private RecyclerView recyclerViewGlobalPostList;
    private ArrayList<Post> globalPostList;
    private GlobalPostAdapter globalPostAdapter;
    private GridLayoutManager gridLayoutManager;    
    
    private Button buttonSignOut, buttonPost, buttonMyFeed;
    private String uID;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        uID = mAuth.getCurrentUser().getUid();

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        globalPostList = new ArrayList <Post> ();

        buttonSignOut = (Button) findViewById(R.id.buttonSignOut);
        buttonPost = (Button) findViewById(R.id.buttonPost);
        buttonMyFeed = (Button) findViewById(R.id.buttonMyFeed);
        
        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Sign out and then go to login page
                    mAuth.signOut();
                    startActivity(new Intent(GlobalActivity.this, MainActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        buttonMyFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GlobalActivity.this, ProfileActivity.class));
            }
        });

        recyclerViewGlobalPostList = (RecyclerView) findViewById(R.id.recyclerViewGlobalPostList);
        gridLayoutManager = new GridLayoutManager(this, 1,GridLayoutManager.VERTICAL,false);
        recyclerViewGlobalPostList.setLayoutManager(gridLayoutManager);

        loadGlobalPosts();
    }

    private void loadGlobalPosts() {

        globalPostList.clear();
        CollectionReference collectionReference = db.collection("photos").document(uID).collection("posts");
        collectionReference.orderBy("timeStamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Post post = document.toObject(Post.class);
                                globalPostList.add(post);
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        Log.d(TAG, "all global posts:" + globalPostList.toString());
                        globalPostAdapter = new GlobalPostAdapter(GlobalActivity.this, globalPostList);
                        recyclerViewGlobalPostList.setAdapter(globalPostAdapter);
                        recyclerViewGlobalPostList.setHasFixedSize(true);
                    }
                });

    }
}