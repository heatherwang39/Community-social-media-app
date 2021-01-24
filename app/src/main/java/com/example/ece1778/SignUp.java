package com.example.ece1778;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class SignUp extends AppCompatActivity {

    public void signup(View view){
        Log.i("Info","Signup Button in signup page is pressed!");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }
}