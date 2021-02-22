package com.example.ece1778;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;

public class ProfileFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private View rootView;
    private TextView textViewUsername,textViewBio;
    private ImageView profileImage;
    private String uID;
    private Button buttonSignOut;

    private RecyclerView recyclerViewPostList;
    private ArrayList<Post> postList;
    private PostAdapter postAdapter;
    private GridLayoutManager gridLayoutManager;
    private String currentPhotoPath;

    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        uID = mAuth.getCurrentUser().getUid();

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        // Initialize Storage
        storage = FirebaseStorage.getInstance();

        postList = new ArrayList <Post> ();

        textViewUsername = (TextView) rootView.findViewById(R.id.textViewUsername);
        textViewBio = (TextView) rootView.findViewById(R.id.textViewBio);
        profileImage = (ImageView) rootView.findViewById(R.id.profileImage);

        buttonSignOut = (Button) rootView.findViewById(R.id.buttonSignOut);
        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Sign out and then go to login page
                    mAuth.signOut();
                    startActivity(new Intent(getActivity(), MainActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        context = getActivity();

        if(mAuth.getCurrentUser() == null){
            startActivity(new Intent(getActivity(), MainActivity.class));
        }else{
            showProfile();
        }

        return rootView;
    }

    private void showProfile() {
        DocumentReference documentReference = db.collection("users").document(uID);
        documentReference.addSnapshotListener( new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Log.d("Profile","-->"+e.toString());
                    return;
                }

                if(documentSnapshot.exists()){
                    String username = documentSnapshot.getString("username");
                    textViewUsername.setText(username);
                    String bio = documentSnapshot.getString("bio");
                    textViewBio.setText(bio);
                    String profilePic = documentSnapshot.getString("displayPicPath");
                    Glide.with(getActivity()).load(profilePic).into(profileImage);
                    Log.i("Profile","Show profile successfully"+username+" "+bio +uID);
                }
            }
        });

        //load the post pics
        recyclerViewPostList = (RecyclerView) rootView.findViewById(R.id.recyclerViewPostList);
        gridLayoutManager = new GridLayoutManager(getActivity(), 3,GridLayoutManager.VERTICAL,false);
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

}
