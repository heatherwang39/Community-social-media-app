package com.example.ece1778;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class Caption extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private static final String TAG = "Caption";
    private String uID, currentPhotoPath, timeStamp, caption, hashTags;
    private Bitmap postBitmap;
    private ImageView postImage;
    private Button buttonCancelCaption, buttonPostCaption;
    private EditText editTextCaption;
    private Switch switchHashTag;

    private Uri postURI;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

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
        switchHashTag = (Switch) findViewById(R.id.switchHashTag);

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

        switchHashTag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    labelImage(isChecked);
                } else {
                    // The toggle is disabled
                    labelImage(isChecked);
                }
            }
        });


        //If the user cancelled taking a picture, or want to change the picture
        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        //this page will start from taking a picture
        takePicture();
    }

    private void labelImage(boolean isChecked) {
        if (isChecked) {
            //Start a new line for hashTags
            hashTags = "\n";

            //Create a FirebaseVisionImage object from a Bitmap object
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(postBitmap);

            // Set the minimum confidence required:
            FirebaseVisionOnDeviceImageLabelerOptions options =
                    new FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                            .setConfidenceThreshold(0.7f)
                            .build();
            FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                    .getOnDeviceImageLabeler(options);

            labeler.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                            // Task completed successfully
                            for (FirebaseVisionImageLabel label : labels) {
                                String text = label.getText();
                                String entityId = label.getEntityId();
                                float confidence = label.getConfidence();
                                Log.e(TAG, "Hashtags: " + entityId + "  ** text:" + text + " ** confidence" + confidence);
                                hashTags = hashTags + "#" + text + " ";
                            }
                            caption = editTextCaption.getText().toString();
                            editTextCaption.setText(caption + hashTags);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            Toast.makeText(Caption.this, "Unable to generate hash tags",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            caption = editTextCaption.getText().toString();
            //Delete the auto generated tags(they should be in a new line), keep those typed by user himself
            if (caption.contains("/n")) {
                String[] textWithoutAutoTags = caption.split("/n");
                editTextCaption.setText(textWithoutAutoTags[0]);
            }
        }
    }

    private void takePicture() {
        Log.d(TAG,"The user is taking a picture!");
        Intent makePostIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (makePostIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(Caption.this, "Error when creating the Post File",
                        Toast.LENGTH_LONG).show();
            }
            if (photoFile != null) {
                postURI = FileProvider.getUriForFile(this,"com.example.android.fileprovider",photoFile);
//                getCtx().setPostUri(postURI);
                makePostIntent.putExtra(MediaStore.EXTRA_OUTPUT, postURI);
                startActivityForResult(makePostIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Log.i(TAG, "onActivityResult: Make Post Image Capture RESULT OK");
            try {
                postBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), postURI);
                postImage.setImageBitmap(postBitmap);
                Log.i(TAG, "onActivityResult ok: get postBitmap successfully");
//                getCtx().setPostBitmap(postBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "onActivityResult ok: get postBitmap unsuccessfully");
            }}else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_CANCELED){
            Log.i(TAG, "onActivityResult: Make Post Image Capture RESULT CANCELLED");
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
//        getCtx().setCurrentPhotoPath(currentPhotoPath);
        return image;
    }

    private void uploadPost() throws IOException {
         // crop the picture to square
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
                        Intent intent = new Intent(Caption.this, BottomNavigationActivity.class);
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

//    public CurrentPost getCtx(){
//        return ((CurrentPost) getApplicationContext());
//    }




}