package com.example.ambulancehotline;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

public class RegisterActivty extends AppCompatActivity {

    FloatingActionButton registerBackFab;
    EditText emailSignUp,usernameSignUp,passwordSignUp,confirmPasswordSignUp;
    Button signUpButton;
    CheckBox showPasswordSignUp;
    String emailText,usernameText,passwordText,confirmPasswordText;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseUser firebaseUser;
    String userId;
    private DatabaseReference root = db.getReference().child("Users");
    ProgressDialog registerDialog;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    //"(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{8,}" +               //at least 8 characters
                    "$");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().hide();
        registerDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        registerBackFab = findViewById(R.id.back_fab);
        emailSignUp = findViewById(R.id.email1);
        usernameSignUp = findViewById(R.id.username);
        passwordSignUp = findViewById(R.id.pass1);
        confirmPasswordSignUp = findViewById(R.id.cpass);
        signUpButton = findViewById(R.id.sign_up2);
        showPasswordSignUp = findViewById(R.id.showpassword2);
        registerBackFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivty.this,SignInActivity.class));
                finish();
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerDialog.setMessage("Registering...");
                registerDialog.show();
                emailText = emailSignUp.getText().toString().trim();
                usernameText = usernameSignUp.getText().toString();
                passwordText = passwordSignUp.getText().toString().trim();
                confirmPasswordText = confirmPasswordSignUp.getText().toString().trim();
                if (usernameText.length() == 0) {
                    usernameSignUp.setError("Input Field Required");
                } if (emailText.length() == 0) {
                    emailSignUp.setError("Input Field Required");
                } if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                    emailSignUp.setError("Input a valid email");
                } if (passwordText.length() == 0) {
                    passwordSignUp.setError("Input Field Required");
                } if (!PASSWORD_PATTERN.matcher(passwordText).matches()){
                    passwordSignUp.setError("Password too weak");
                } if (confirmPasswordText.length() == 0) {
                    confirmPasswordSignUp.setError("Input Field Required");
                }
                if (passwordText.equals(confirmPasswordText)){
                    firebaseAuth.createUserWithEmailAndPassword(emailText,passwordText)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        registerDialog.dismiss();
                                        submitDetails();
                                        startActivity(new Intent(RegisterActivty.this,LoginActivity.class));
                                        finish();
                                    }else {
                                        if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                            Toast.makeText(RegisterActivty.this, "Account already Exists", Toast.LENGTH_SHORT).show();
                                        } else{
                                            Toast.makeText(RegisterActivty.this,"Please check your internet connection",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterActivty.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    confirmPasswordSignUp.setError("Passwords do not match");
                }
            }
        });
        showPasswordSignUp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    passwordSignUp.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    confirmPasswordSignUp.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    passwordSignUp.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    confirmPasswordSignUp.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

    }

    private void submitDetails() {
        firebaseUser = firebaseAuth.getCurrentUser();
        userId = firebaseUser.getUid();
        HashMap<String,String> userData = new HashMap<>();
        userData.put("username",usernameText);
        userData.put("email",emailText);
        userData.put("password",passwordText);
        root.child(userId).setValue(userData);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(RegisterActivty.this,SignInActivity.class));
        finish();
    }
}