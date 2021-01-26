package com.example.ece1778;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button buttonSignUp;
    private Button buttonLogin;

    public void login(View view){
        Log.i("Info","Login Button pressed!");
    }

    public void signup(View view){
        Log.i("Info","Signup Button pressed!");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSignUp = (Button) findViewById(R.id.buttonSignUp);
        buttonSignUp.setOnClickListener(this);

        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonSignUp:
                startActivity(new Intent(this, SignUp.class));
                break;
            case R.id.buttonLogin:
                login();
                break;
        }
    }

    public void login(){
        Log.i("Info","Login Button pressed!");
    }
}