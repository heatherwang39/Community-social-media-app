package com.example.ece1778;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class Comment extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ImageView imageViewComment,deletePost,sendComment;
    private TextView textViewCaption;
    private RecyclerView recyclerViewCommentList;
    private EditText editTextComment;
    private GridLayoutManager gridLayoutManager;
    private String commenterUID, commenterUsername, commenterProfile, commentContent, timeStamp;
    private String postUID, postURL, postCaption;
    private static final String TAG = "Comment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        commenterUID = mAuth.getCurrentUser().getUid();

        // Initialize Cloud FireStore
        db = FirebaseFirestore.getInstance();

        imageViewComment = (ImageView) findViewById(R.id.imageViewComment);
        textViewCaption = (TextView) findViewById(R.id.textViewCaption);
        editTextComment = (EditText) findViewById(R.id.editTextComment);
        deletePost = (ImageView) findViewById(R.id.deletePost);
        sendComment = (ImageView) findViewById(R.id.sendComment);

        deletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"deletePost is clicked!");
            }
        });

        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();
                Log.i(TAG,"sendComment is clicked!");
            }
        });

        //show post image and caption
        if(getIntent().hasExtra("postURL")){
            postURL = getIntent().getStringExtra("postURL");
            Glide.with(Comment.this).load(postURL).into(imageViewComment);
        }

        if(getIntent().hasExtra("uID")){
            postUID = getIntent().getStringExtra("uID");
            Log.i(TAG,"this post belongs to"+postUID);
        }

        if(getIntent().hasExtra("caption")){
            postCaption = getIntent().getStringExtra("caption");
            textViewCaption.setText(postCaption);
        }

        //set comment list recycler view and layout manager
        recyclerViewCommentList = (RecyclerView) findViewById(R.id.recyclerViewCommentList);
        gridLayoutManager = new GridLayoutManager(this, 1,GridLayoutManager.VERTICAL,false);
        recyclerViewCommentList.setLayoutManager(gridLayoutManager);

    }

    private void sendComment(){
        commentContent = editTextComment.getText().toString();
        DocumentReference documentReference = db.collection("users").document(commenterUID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                commenterUsername = documentSnapshot.getString("username");
                commenterProfile = documentSnapshot.getString("displayPicPath");

//                timeStamp = String.valueOf(System.currentTimeMillis());
                timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());

                Map<String, Object> comment = new HashMap<>();
                comment.put("uID", commenterUID);
                comment.put("username", commenterUID);
                comment.put("displayPicPath", commenterProfile);
                comment.put("commentContent", commentContent);
                comment.put("timestamp", timeStamp);

                db.collection("comments").document(postUID).collection("allComments")
                        .add(comment)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "Successfully adding a new comment.");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding a new comment", e);
                            }
                        });
            }
        });

        editTextComment.setText("");
    }




}