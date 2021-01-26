package com.example.ece1778;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class Profile extends AppCompatActivity {
    private Bitmap imageBitmap;
    private TextView textViewUsername,textViewBio;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        textViewUsername = (TextView) findViewById(R.id.textViewUsername);
        textViewBio = (TextView) findViewById(R.id.textViewBio);
        profileImage = (ImageView) findViewById(R.id.profileImage);

        Intent intent = getIntent();
        String username = (String) intent.getStringExtra("username");
        textViewUsername.setText(username);
        String bio = (String) intent.getStringExtra("bio");
        textViewBio.setText(bio);
        imageBitmap = (Bitmap) intent.getParcelableExtra("imageBitmap");
        profileImage.setImageBitmap(imageBitmap);
        Log.i("Profile", username+" "+bio);


    }
}