package com.example.ece1778;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button register;

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

        register = (Button) findViewById(R.id.signupButton);
        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.signupButton:
                startActivity(new Intent(this, SignUp.class));
                break;
        }
    }
}