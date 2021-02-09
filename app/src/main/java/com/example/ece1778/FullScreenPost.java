package com.example.ece1778;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

public class FullScreenPost extends AppCompatActivity {
    private ImageView imageViewFull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_post);
        imageViewFull = findViewById(R.id.imageViewFull);
        if(getIntent().hasExtra("postURL")){
            String postURL = getIntent().getStringExtra("postURL");
//            Picasso.get().load(postURL).into(imageViewFull);
            Glide.with(FullScreenPost.this).load(postURL).into(imageViewFull);
        }
    }

    public void disappear(View view) {
        finish();
    }
}