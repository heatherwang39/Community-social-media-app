package com.example.ece1778;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Caption extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private static final String TAG = "Caption";
    private String uID, currentPhotoPath, timeStamp, caption;
    private Bitmap postBitmap;
    private ImageView postImage;
    private Button buttonCancelCaption, buttonPostCaption;
    private EditText editTextCaption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caption);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        uID = mAuth.getCurrentUser().getUid();

        // Initialize Cloud Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Storage
        storage = FirebaseStorage.getInstance();

        postImage = (ImageView) findViewById(R.id.postImage);
        editTextCaption = (EditText) findViewById(R.id.editTextCaption);
        buttonCancelCaption = (Button) findViewById(R.id.buttonCancelCaption);
        buttonPostCaption = (Button) findViewById(R.id.buttonPostCaption);

        buttonCancelCaption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonPostCaption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    uploadPost();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //set post imageView and currentPhotoPath based on Application Context
        postBitmap = getCtx().getPostBitmap();
        postImage.setImageBitmap(postBitmap);
        currentPhotoPath = getCtx().getCurrentPhotoPath();
    }


    private void uploadPost() throws IOException {

        // Crop the image to square
        Bitmap square = cropToSquare(postBitmap);

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
                Toast.makeText(Caption.this, "onSuccess: Image Posted",
                        Toast.LENGTH_SHORT).show();
                postReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "onSuccess: get uri "+uri);
                        addToUserPosts(uri);
                        Intent intent = new Intent(Caption.this, ProfileActivity.class);
                        startActivity(intent);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "OnFailure: ", e.getCause());
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

        caption = editTextCaption.getText().toString();

        //I don't use custom object post here because all field names are converted to lowercase automatically, like uid and timestamp
        post.put("uID", uID);
        post.put("storageRef", String.valueOf(uri));
        post.put("timeStamp", timeStamp);
        post.put("caption", caption);

        db.collection("photos")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "On Success: addToUserPosts" + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error addToUserPosts", e);
                    }
                });
    }

    public CurrentPost getCtx(){
        return ((CurrentPost) getApplicationContext());
    }




}