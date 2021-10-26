package com.example.ambulancehotline;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgotPasswordActivity extends AppCompatActivity {

    FloatingActionButton forgotPasswordBackFab;
    EditText forgotPasswordEmail;
    Button next,close;
    TextView switchAccounts,emailTV;
    Dialog dialog;
    LottieAnimationView emailsent;
    String emailStr;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        getSupportActionBar().hide();

        dialog = new Dialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        forgotPasswordBackFab = findViewById(R.id.back3_fab);
        forgotPasswordEmail = findViewById(R.id.email6);
        next = findViewById(R.id.next);
        switchAccounts = findViewById(R.id.switchaccounts);

        forgotPasswordBackFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ForgotPasswordActivity.this,SignInActivity.class));
                finish();
            }
        });
        switchAccounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ForgotPasswordActivity.this,SignInActivity.class));
                finish();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailStr = forgotPasswordEmail.getText().toString().trim();
                if (emailStr.length() == 0){
                    forgotPasswordEmail.setError("Input Field Required");

                }else{
                    forgotPasswordEmail.getText().clear();
                    firebaseAuth.sendPasswordResetEmail(emailStr)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    openDialogBox();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ForgotPasswordActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void openDialogBox() {
        dialog.setContentView(R.layout.custom_email_dialog_box);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        emailTV = dialog.findViewById(R.id.actual_email);
        emailTV.setText(emailStr);
        close = dialog.findViewById(R.id.btn_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(ForgotPasswordActivity.this,LoginActivity.class));
                finish();
            }
        });
        emailsent = dialog.findViewById(R.id.reset_email);
        emailsent.playAnimation();
        emailsent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailsent.playAnimation();
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ForgotPasswordActivity.this,LoginActivity.class));
        finish();
    }
}