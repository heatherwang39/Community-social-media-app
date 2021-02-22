package com.example.ece1778;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class GlobalFragment extends Fragment {

    private static final String TAG = "Global";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private RecyclerView recyclerViewGlobalPostList;
    private ArrayList<Post> globalPostList;
    private GlobalPostAdapter globalPostAdapter;
    private GridLayoutManager gridLayoutManager;

    private Button buttonSignOut;
    private String uID;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_global, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        uID = mAuth.getCurrentUser().getUid();

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        globalPostList = new ArrayList <Post> ();

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

        recyclerViewGlobalPostList = (RecyclerView) rootView.findViewById(R.id.recyclerViewGlobalPostList);
        gridLayoutManager = new GridLayoutManager(getActivity(), 1,GridLayoutManager.VERTICAL,false);
        recyclerViewGlobalPostList.setLayoutManager(gridLayoutManager);

        loadGlobalPosts();

        return rootView;
    }

    private void loadGlobalPosts() {
        globalPostList.clear();
        CollectionReference collectionReference = db.collection("photos");
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
                        globalPostAdapter = new GlobalPostAdapter(getActivity(), globalPostList);
                        recyclerViewGlobalPostList.setAdapter(globalPostAdapter);
                        recyclerViewGlobalPostList.setHasFixedSize(true);
                    }
                });

    }

}
