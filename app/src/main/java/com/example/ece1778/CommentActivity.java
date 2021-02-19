package com.example.ece1778;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class CommentActivity extends AppCompatActivity {
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
    private ArrayList<Comment> commentList;
    private CommentAdapter commentAdapter;
    private Context context;

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

        commentList = new ArrayList<Comment>();
        context = CommentActivity.this;

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
            Glide.with(CommentActivity.this).load(postURL).into(imageViewComment);
        }

        if(getIntent().hasExtra("uID")){
            postUID = getIntent().getStringExtra("uID");
            Log.i(TAG,"this post belongs to: "+postUID);
        }

        if(getIntent().hasExtra("caption")){
            postCaption = getIntent().getStringExtra("caption");
            textViewCaption.setText(postCaption);
        }

        //set comment list recycler view and layout manager
        recyclerViewCommentList = (RecyclerView) findViewById(R.id.recyclerViewCommentList);
        gridLayoutManager = new GridLayoutManager(this, 1,GridLayoutManager.VERTICAL,false);
        recyclerViewCommentList.setLayoutManager(gridLayoutManager);

        //load comments
        loadComments();
    }

    private void loadComments() {
        commentList.clear();
        CollectionReference collectionReference = db.collection("comments").document(postUID).collection("allComments");
        collectionReference.orderBy("timeStamp", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Comment comment = document.toObject(Comment.class);
                                commentList.add(comment);
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d("Profile", "Error getting documents: ", task.getException());
                        }
                        commentAdapter = new CommentAdapter(context, commentList);
                        recyclerViewCommentList.setAdapter(commentAdapter);
                        recyclerViewCommentList.setHasFixedSize(true);
                    }
                });
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

                //add custom object: comment
                Comment comment = new Comment(commenterUID, commenterUsername, commenterProfile,
                        commentContent, timeStamp);

                db.collection("comments").document(postUID).collection("allComments")
                        .add(comment)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "Successfully adding a new comment. Will reload the comments!");
                                //reload comments
                                loadComments();
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