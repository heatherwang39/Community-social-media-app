package com.example.ece1778;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class Profile extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private TextView textViewUsername,textViewBio;
    private ImageView profileImage;
    private String uID;
    private Button buttonSignOut;
    private Button buttonPost;

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private RecyclerView recyclerViewPostList;
    private ArrayList<Post> postList;
    private Adapter adapter;
    private GridLayoutManager gridLayoutManager;
    private String currentPhotoPath, timeStamp;

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

        textViewUsername = (TextView) findViewById(R.id.textViewUsername);
        textViewBio = (TextView) findViewById(R.id.textViewBio);
        profileImage = (ImageView) findViewById(R.id.profileImage);

        buttonSignOut = (Button) findViewById(R.id.buttonSignOut);
        buttonSignOut.setOnClickListener(this);

        postList = new ArrayList <Post> ();
        buttonPost = (Button) findViewById(R.id.buttonPost);
        buttonPost.setOnClickListener(this);

        context = Profile.this;

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
        }
    }

    private void showProfile() {
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
                    String profilePic = documentSnapshot.getString("displayPicPath");
                    Glide.with(Profile.this).load(profilePic).into(profileImage);
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
        CollectionReference collectionReference = db.collection("photos").document(uID).collection("posts");
        collectionReference.orderBy("timeStamp", Query.Direction.DESCENDING)
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
                        adapter = new Adapter(context, postList);
                        recyclerViewPostList.setAdapter(adapter);
                        recyclerViewPostList.setHasFixedSize(true);
                    }
                });
    }

    private void makePost() {
        Log.d("Profile","makePost button is clicked!");
        Intent makePostIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (makePostIntent.resolveActivity(getPackageManager()) != null) {
            File postFile = null;
            try {
                postFile = createPostFile();
            } catch (IOException ex) {
                Toast.makeText(Profile.this, "Error when creating the Post File",
                        Toast.LENGTH_LONG).show();
            }
            if (postFile != null) {
                postURI = FileProvider.getUriForFile(this,"com.example.android.fileprovider",postFile);
                makePostIntent.putExtra(MediaStore.EXTRA_OUTPUT, postURI);
                startActivityForResult(makePostIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE){
            switch (resultCode){
                case RESULT_OK:
                    Log.i("Profile", "onActivityResult: Make Post Image Capture RESULT OK");
                    Bitmap bitmapConvert = null;
                    try {
                        bitmapConvert = MediaStore.Images.Media.getBitmap(this.getContentResolver(), postURI);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        uploadPost(bitmapConvert);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case RESULT_CANCELED:
                    Log.i("Profile", "onActivityResult: Make Post Image Capture RESULT CANCELLED");
                    break;
                default:
                    break;

            }
        }
    }

    private File createPostFile() throws IOException {
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
//        getApp().setCurrentPhoto(currentPhotoPath);
        return image;
    }

    private void uploadPost(Bitmap bitmap) throws IOException {
        // Crop the image to square
        Bitmap square = cropToSquare(bitmap);

        //Rotate the image
        ExifInterface ei = new ExifInterface(currentPhotoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        Bitmap rotatedBitmap = null;
        Matrix matrix = new Matrix();
        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
        }
        rotatedBitmap = Bitmap.createBitmap(square, 0, 0, square.getWidth(), square.getHeight(),
                matrix, true);

        // Downscale to 1024*1024
        Bitmap finalBitmap = Bitmap.createScaledBitmap(rotatedBitmap, 1024, 1024, true);

        //upload
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        timeStamp = String.valueOf(System.currentTimeMillis());
        final StorageReference postReference = storage.getReference().child("photos").child(uID+"/"+timeStamp+".jpeg");

        postReference.putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(Profile.this, "onSuccess: Image Posted",
                        Toast.LENGTH_SHORT).show();
                postReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("Profile", "onSuccess: get uri "+uri);
                        addToUserPosts(uri);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Profile", "OnFailure: ", e.getCause());
            }
        });
    }

    public static Bitmap cropToSquare(Bitmap bitmap){
        Bitmap cropImg;
        if (bitmap.getWidth() >= bitmap.getHeight()){
            cropImg = Bitmap.createBitmap(bitmap,bitmap.getWidth()/2 - bitmap.getHeight()/2,0,
                    bitmap.getHeight(),bitmap.getHeight());
        }else{
            cropImg = Bitmap.createBitmap(bitmap,0,bitmap.getHeight()/2 - bitmap.getWidth()/2,
                    bitmap.getWidth(),bitmap.getWidth());
        }
        return cropImg;
    }


    private void addToUserPosts(Uri uri){
        Map<String, Object> post = new HashMap<>();
        post.put("uID", uID);
        post.put("storageRef", String.valueOf(uri));
        post.put("timeStamp", timeStamp);

        db.collection("photos").document(uID).collection("posts")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Profile", "On Success: addToUserPosts" + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Profile", "Error addToUserPosts", e);
                    }
                });
        //update the posts shown in user's profile page
        loadPosts();
    }
}