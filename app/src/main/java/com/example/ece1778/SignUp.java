package com.example.ece1778;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class SignUp extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String TAG = "EmailPassword";
    private TextView textViewBanner;
    private Button signUp;
    private EditText editTextEmail, editTextPassword, editTextPassword2, editTextName, editTextBio;
    private ProgressBar progressBar;
    private String uID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Cloud Firestore
        db = FirebaseFirestore.getInstance();

        textViewBanner = (TextView) findViewById(R.id.textViewBanner);
        textViewBanner.setOnClickListener(this);

        signUp = (Button) findViewById(R.id.buttonSignUp);
        signUp.setOnClickListener(this);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextPassword2 = (EditText) findViewById(R.id.editTextPassword2);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextBio = (EditText) findViewById(R.id.editTextBio);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

    }

    // [START on_start_check_user]
    // Cite: https://github.com/firebase/quickstart-android/blob/256c7e1e6e1dd2be7025bb3f858bf906fd158fa0/auth/app/src/main/java/com/google/firebase/quickstart/auth/java/EmailPasswordActivity.java#L46-L46
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }
    // [END on_start_check_user]

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textViewBanner:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.buttonSignUp:
                signUp();
                break;
        }
    }

    public void signUp(){

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString();
        String password2 = editTextPassword2.getText().toString();
        String name = editTextName.getText().toString().trim();
        String bio = editTextBio.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Valid Email Address is required!");
            editTextEmail.requestFocus();
            return;
        }

        if(password.length()<6){
            editTextPassword.setError("Password should be at least 6 characters.");
            editTextPassword.requestFocus();
            return;
        }

        if(!password.equals(password2)){
            editTextPassword2.setError("The password you entered doesn't match!");
            editTextPassword2.requestFocus();
            return;
        }
        Log.i("Info","Signup Button in signup page is pressed!");
        progressBar.setVisibility(View.VISIBLE);

        // [START create_user_with_email]
        // Cite: https://github.com/firebase/quickstart-android/blob/256c7e1e6e1dd2be7025bb3f858bf906fd158fa0/auth/app/src/main/java/com/google/firebase/quickstart/auth/java/EmailPasswordActivity.java#L229
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(SignUp.this, "User created.",Toast.LENGTH_LONG).show();
                            //FirebaseUser user = mAuth.getCurrentUser();
                            uID = mAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = db.collection("users").document(uID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("email",email);
                            user.put("name",name);
                            user.put("bio",bio);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "success: user profile is created for"+uID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "failure: "+e.toString());
                                }
                            });
                            //startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            progressBar.setVisibility(View.GONE);
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUp.this, "Authentication failed.",Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                            //updateUI(null);
                        }
                        // [START_EXCLUDE]
                        //hideProgressBar();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]

    }

}