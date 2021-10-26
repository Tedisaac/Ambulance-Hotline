package com.example.ambulancehotline;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_CODE = 100;
    Uri uriProfileImage;

    CircleImageView userProfileImage;
    TextView userNameText;
    FloatingActionButton profileBack;
    RelativeLayout changeUserName, changePassword, deleteAccount, logOut;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    ;
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    ;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private StorageReference imageReference = FirebaseStorage.getInstance().getReference();
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String NAME_STRING = "name";
    private static final String PROFILE_IMAGE_STRING = "image";
    GoogleSignInClient googleSignInClient;

    private DatabaseReference reference = database.getReference("Users");
    AlertDialog.Builder builder;
    Dialog deleteDialog;
    ProgressDialog dialog;
    String name1str;
    String pImageUrl;
    String sharedName;
    String sharedPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().hide();
        logOutUser();
        saveProfileData();
        loadProfileData();


        builder = new AlertDialog.Builder(this);
        deleteDialog = new Dialog(this);
        dialog = new ProgressDialog(this);

        profileBack = findViewById(R.id.back_fab_profile);
        userProfileImage = findViewById(R.id.profile_image_view);
        userNameText = findViewById(R.id.username_id);
        if (firebaseUser.getDisplayName() != null && firebaseUser.getPhotoUrl() != null) {
            String googlename = firebaseUser.getDisplayName();
            Log.e(TAG, "getUserName: " + googlename);
            Log.e(TAG, "getUserName: " + firebaseUser.getPhotoUrl());
            userNameText.setText(googlename);
            reference.child(firebaseUser.getUid()).child("vimage").child("imageUrl").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String profilePic2 = snapshot.getValue(String.class);
                    Glide.with(ProfileActivity.this)
                            .load(profilePic2)
                            .into(userProfileImage);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else {
            getUserName();
            getProfileImage();
        }
        changeUserName = findViewById(R.id.change_username_layout);
        changePassword = findViewById(R.id.change_password_layout);
        deleteAccount = findViewById(R.id.delete_account_layout);
        logOut = findViewById(R.id.logout_account_layout);
        profileBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                finish();
            }
        });
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageSelector();
            }
        });
        changeUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view2 = LayoutInflater.from(getApplicationContext()).inflate(R.layout.change_name, null);
                EditText newName = view2.findViewById(R.id.new_name_ed);
                Button updateName = view2.findViewById(R.id.update_name);
                builder.setView(view2);
                AlertDialog nameDialog = builder.create();
                nameDialog.show();
                updateName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.setMessage("Updating Username...");
                        dialog.show();
                        String nameStr = newName.getText().toString().trim();
                        if (TextUtils.isEmpty(nameStr)) {
                            newName.setError("Please input name");
                        }
                        if (firebaseUser != null) {
                            if (firebaseUser.getDisplayName() != null) {
                                UserProfileChangeRequest name = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(nameStr)
                                        .build();
                                firebaseUser.updateProfile(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            dialog.dismiss();
                                            nameDialog.dismiss();
                                            reference.child(firebaseUser.getUid()).child("username").setValue(nameStr);
                                            userNameText.setText(firebaseUser.getDisplayName());
                                            Log.e(TAG, "onComplete: " + firebaseUser.getDisplayName());
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "onFailure: " + e.toString());
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "No user", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view1 = LayoutInflater.from(getApplicationContext()).inflate(R.layout.change_password, null);
                EditText currentPassword = view1.findViewById(R.id.current_password_ed);
                EditText newPassword = view1.findViewById(R.id.new_password_ed);
                CheckBox currCB = view1.findViewById(R.id.show_current_password);
                CheckBox newCB = view1.findViewById(R.id.show_new_password);
                currCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if (isChecked) {
                            currentPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        } else {
                            currentPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        }
                    }
                });
                newCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if (isChecked) {
                            newPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        } else {
                            newPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        }
                    }
                });
                Button update = view1.findViewById(R.id.update_password);
                builder.setView(view1);
                AlertDialog passDialog = builder.create();
                passDialog.show();
                update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String currentPassString = currentPassword.getText().toString().trim();
                        String newPassString = newPassword.getText().toString().trim();
                        if (TextUtils.isEmpty(currentPassString)) {
                            currentPassword.setError("Input current password");
                            return;
                        }
                        if (TextUtils.isEmpty(newPassString)) {
                            newPassword.setError("input new password");
                            return;
                        }
                        passDialog.dismiss();
                        updatePassword(currentPassString, newPassString);
                    }
                });
            }
        });
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDialogBox();
            }
        });
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setMessage("Logging out");
                dialog.show();
                googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            firebaseAuth.signOut();
                            Toast.makeText(ProfileActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: " + e.toString());
                    }
                });
            }
        });
    }

    private void loadProfileData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        sharedName = sharedPreferences.getString(NAME_STRING, "");
        sharedPic = sharedPreferences.getString(PROFILE_IMAGE_STRING, "");
    }

    private void saveProfileData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(NAME_STRING, name1str);
        editor.putString(PROFILE_IMAGE_STRING, pImageUrl);
        editor.apply();
    }

    private void getUserName() {
        if (firebaseUser != null) {
            reference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        name1str = snapshot.child("username").getValue(String.class);
                        Log.e(TAG, "onDataChange: " + name1str);
                        UserProfileChangeRequest nameStr1 = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name1str)
                                .build();
                        firebaseUser.updateProfile(nameStr1)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.e(TAG, "onComplete: " + firebaseUser.getDisplayName());
                                            userNameText.setText(firebaseUser.getDisplayName());
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "onFailure: " + e.toString()
                                );
                            }
                        });

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }

    }

    private void getProfileImage() {
        if (firebaseUser.getPhotoUrl() == null) {
            reference.child(firebaseUser.getUid()).child("vimage").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        pImageUrl = snapshot.child("imageUrl").getValue(String.class);
                        Log.e(TAG, "onDataChange: " + pImageUrl);
                        Glide.with(ProfileActivity.this)
                                .load(pImageUrl)
                                .into(userProfileImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "onCancelled: " + error.toString());
                }
            });
        }

    }


    private void imageSelector() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Choose Profile Picture"), PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriProfileImage = data.getData();
            UserProfileChangeRequest pp = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(uriProfileImage)
                    .build();
            firebaseUser.updateProfile(pp)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.e(TAG, "onComplete: " + firebaseUser.getPhotoUrl());
                            }
                        }
                    });
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                userProfileImage.setImageBitmap(bitmap);
                uploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        if (uriProfileImage != null) {
            StorageReference fileRef = imageReference.child(System.currentTimeMillis() + "." + getFileExtension(uriProfileImage));
            fileRef.putFile(uriProfileImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            dialog.dismiss();
                            Model model = new Model(uri.toString());
                            //String modelId = reference.child(firebaseUser.getUid()).getKey();
                            reference.child(firebaseUser.getUid()).child("vimage").setValue(model);
                            Toast.makeText(ProfileActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    dialog.setMessage("Uploading Image...");
                    dialog.show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getFileExtension(Uri mUri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }


    private void logOutUser() {
        googleSignInClient = GoogleSignIn.getClient(ProfileActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);
    }

    private void deleteDialogBox() {
        deleteDialog.setContentView(R.layout.delete_account);
        deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button yes = deleteDialog.findViewById(R.id.btn_yes);
        Button no = deleteDialog.findViewById(R.id.btn_no);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDialog.dismiss();
                dialog.setMessage("Deleting Account...");
                dialog.show();
                reference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String passStr1 = snapshot.child("password").getValue(String.class);
                            Log.e(TAG, "onDataChange: " + passStr1);
                            AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), passStr1);
                            firebaseUser.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            dialog.dismiss();
                                                            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                                                            finish();
                                                        }
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e(TAG, "onFailure: " + e.toString());
                                                    }
                                                });
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "onFailure: " + e.toString());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDialog.dismiss();
            }
        });
        deleteDialog.show();
    }

    private void updatePassword(String currentPassString, String newPassString) {
        dialog.setMessage("Updating password...");
        dialog.show();

        AuthCredential auth = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassString);
        firebaseUser.reauthenticate(auth)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ProfileActivity.this, "authentication success", Toast.LENGTH_SHORT).show();
                        firebaseUser.updatePassword(newPassString).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                dialog.dismiss();
                                reference.child(firebaseUser.getUid()).child("password").setValue(newPassString);
                                Toast.makeText(ProfileActivity.this, "Password updated", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(ProfileActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(ProfileActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        finish();
    }
}