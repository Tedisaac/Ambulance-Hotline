package com.example.ambulancehotline;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMarkerClickListener {

    GoogleMap map;
    private int GPS_REQUEST_CODE = 9001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private boolean permissionDenied = false;
    ArrayList<LatLng> arrayList = new ArrayList<>();
    LatLng shooters = new LatLng(-1.2787945377070797, 36.96765591486895);
    LatLng benedicta = new LatLng(-1.2854212568104981, 36.95572096214164);
    LatLng sda = new LatLng(-1.2786208753468251, 36.95225983989076);
    LatLng shell = new LatLng(-1.2830078822588251, 36.94422072316229);
    LatLng utawala = new LatLng(-1.291921296681514, 36.94517237105174);
    LatLng sarit = new LatLng(-1.2617073086243904, 36.803393868997034);
    LatLng jacaranda = new LatLng(-1.2631982118838554, 36.80058773408264);
    LatLng museum = new LatLng(-1.2732254579586302, 36.81350632829122);
    LatLng university_way = new LatLng(-1.2814527142367078, 36.81534951075523);
    private Marker mShooters;
    private Marker mBenedicta;
    private Marker mSda;
    private Marker mUtawala;
    private Marker mShell;
    private Marker mSarit;
    private Marker mJacaranda;
    private Marker mMuseum;
    private Marker mUniversityWay;
    String pnumber, textTitle, textResponse, textArea;
    CircleImageView userImage;
    TextView title, response, area;
    String obj;

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String NAME_STRING = "name";
    private static final String PROFILE_IMAGE_STRING = "image";
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    String imageString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();
        initMap();
        isTelephonyEnabled();
        Log.e(TAG, "onCreate: " + imageString);
        arrayList.add(shooters);
        arrayList.add(sda);
        arrayList.add(shell);
        arrayList.add(utawala);
        arrayList.add(benedicta);
        arrayList.add(sarit);
        arrayList.add(university_way);
        arrayList.add(jacaranda);
        arrayList.add(museum);
        userImage = findViewById(R.id.user_image);
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });
        if (firebaseUser != null && firebaseUser.getPhotoUrl() != null) {
            Glide.with(MainActivity.this)
                    .load(firebaseUser.getPhotoUrl())
                    .into(userImage);
        }
        getImage();
    }

    private void getImage() {
        if (firebaseUser != null && firebaseUser.getPhotoUrl() == null) {
            reference.child("Users").child(firebaseUser.getUid()).child("vimage").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        imageString = snapshot.child("imageUrl").getValue(String.class);
                        Log.e(TAG, "onDataChange: " + imageString);
                        Glide.with(MainActivity.this)
                                .load(imageString)
                                .into(userImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "onCancelled: " + error.toString());
                }
            });
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            addMarkersToMap();
            map.setOnMyLocationButtonClickListener(this);
            map.setOnMyLocationClickListener(this);
            map.setOnMarkerClickListener(this);
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(shell)
                    .include(shooters)
                    .include(sda)
                    .include(utawala)
                    .include(benedicta)
                    .include(sarit)
                    .include(university_way)
                    .include(jacaranda)
                    .include(museum)
                    .build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.12);
            map.animateCamera(CameraUpdateFactory.zoomTo(1500.0f));
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));

        /*for (int i = 0; i < arrayList.size(); i++) {
            map.addMarker(new MarkerOptions().position(arrayList.get(i))
                    .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_baseline_local_hospital_24)));
            map.animateCamera(CameraUpdateFactory.zoomTo(1500.0f));
            map.moveCamera(CameraUpdateFactory.newLatLng(arrayList.get(i)));
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    reference.child("Ambulances").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                LatLng city = new LatLng(37, -122);
                                Log.e(TAG, "onDataChange: " + city);
                                if (Nairobi == city) {
                                    if (snapshot.child("Ambulance1").child("title").getValue(String.class).equalsIgnoreCase("Utawala")) {
                                        String num1 = snapshot.child("Ambulance1").child("number").getValue(String.class);
                                        String title_text = snapshot.child("Ambulance1").child("title").getValue(String.class);
                                        callFuntion(num1);
                                        title.setText(title_text);
                                    }
                                }
                                if (snapshot.child("Ambulance2").child("title").getValue(String.class).equalsIgnoreCase("Shooters")) {
                                    String num2 = snapshot.child("Ambulance2").child("number").getValue(String.class);
                                    String title_text1 = snapshot.child("Ambulance2").child("title").getValue(String.class);
                                    callFuntion(num2);
                                    title.setText(title_text1);
                                }



                           *//* Log.e(TAG, "onDataChange: "+number1 );
                            Log.e(TAG, "onDataChange: "+number2 );

                            title.setText(number2);
                            response.setText(number1);*//*


                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    showDialog();
                    return false;
                }
            });
        }*/


            enableMyLocation();
        } else {
            Log.e(TAG, "Map null");
        }

    }

    private void addMarkersToMap() {
        mShell = map.addMarker(new MarkerOptions()
                .position(shell)
                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_baseline_local_hospital_24)));

        mShooters = map.addMarker(new MarkerOptions()
                .position(shooters)
                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_baseline_local_hospital_24)));

        mUtawala = map.addMarker(new MarkerOptions()
                .position(utawala)
                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_baseline_local_hospital_24)));

        mBenedicta = map.addMarker(new MarkerOptions()
                .position(benedicta)
                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_baseline_local_hospital_24)));

        mSda = map.addMarker(new MarkerOptions()
                .position(sda)
                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_baseline_local_hospital_24)));
        mSarit = map.addMarker(new MarkerOptions()
                .position(sarit)
                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_baseline_local_hospital_24)));
        mUniversityWay = map.addMarker(new MarkerOptions()
                .position(university_way)
                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_baseline_local_hospital_24)));
        mJacaranda = map.addMarker(new MarkerOptions()
                .position(jacaranda)
                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_baseline_local_hospital_24)));
        mMuseum = map.addMarker(new MarkerOptions()
                .position(museum)
                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_baseline_local_hospital_24)));
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        if (marker.equals(mShooters)) {
            reference.child("Ambulances").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        pnumber = snapshot.child("Ambulance1").child("number").getValue(String.class);
                        textTitle = snapshot.child("Ambulance1").child("title").getValue(String.class);
                        textResponse = snapshot.child("Ambulance1").child("response").getValue(String.class);
                        textArea = snapshot.child("Ambulance1").child("hospital").getValue(String.class);
                        Log.e(TAG, "onDataChange: " + pnumber);
                        Log.e(TAG, "onDataChange: " + textTitle);
                        Log.e(TAG, "onDataChange: " + textResponse);
                        title.setText(textTitle);
                        response.setText(textResponse);
                        area.setText(textArea);


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else if (marker.equals(mSda)) {
            reference.child("Ambulances").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        pnumber = snapshot.child("Ambulance2").child("number").getValue(String.class);
                        textTitle = snapshot.child("Ambulance2").child("title").getValue(String.class);
                        textResponse = snapshot.child("Ambulance2").child("response").getValue(String.class);
                        textArea = snapshot.child("Ambulance2").child("hospital").getValue(String.class);
                        Log.e(TAG, "onDataChange: " + pnumber);
                        Log.e(TAG, "onDataChange: " + textTitle);
                        Log.e(TAG, "onDataChange: " + textResponse);
                        title.setText(textTitle);
                        response.setText(textResponse);
                        area.setText(textArea);


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else if (marker.equals(mBenedicta)) {
            reference.child("Ambulances").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        pnumber = snapshot.child("Ambulance3").child("number").getValue(String.class);
                        textTitle = snapshot.child("Ambulance3").child("title").getValue(String.class);
                        textResponse = snapshot.child("Ambulance3").child("response").getValue(String.class);
                        textArea = snapshot.child("Ambulance3").child("hospital").getValue(String.class);
                        Log.e(TAG, "onDataChange: " + pnumber);
                        Log.e(TAG, "onDataChange: " + textTitle);
                        Log.e(TAG, "onDataChange: " + textResponse);
                        title.setText(textTitle);
                        response.setText(textResponse);
                        area.setText(textArea);


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else if (marker.equals(mUtawala)) {
            reference.child("Ambulances").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        pnumber = snapshot.child("Ambulance4").child("number").getValue(String.class);
                        textResponse = snapshot.child("Ambulance4").child("response").getValue(String.class);
                        textTitle = snapshot.child("Ambulance4").child("title").getValue(String.class);
                        textArea = snapshot.child("Ambulance4").child("hospital").getValue(String.class);
                        Log.e(TAG, "onDataChange: " + pnumber);
                        Log.e(TAG, "onDataChange: " + textTitle);
                        Log.e(TAG, "onDataChange: " + textResponse);
                        title.setText(textTitle);
                        response.setText(textResponse);
                        area.setText(textArea);


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else if (marker.equals(mShell)) {
            reference.child("Ambulances").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        pnumber = snapshot.child("Ambulance5").child("number").getValue(String.class);
                        textTitle = snapshot.child("Ambulance5").child("title").getValue(String.class);
                        textResponse = snapshot.child("Ambulance5").child("response").getValue(String.class);
                        textArea = snapshot.child("Ambulance5").child("hospital").getValue(String.class);
                        Log.e(TAG, "onDataChange: " + pnumber);
                        Log.e(TAG, "onDataChange: " + textTitle);
                        Log.e(TAG, "onDataChange: " + textResponse);
                        title.setText(textTitle);
                        response.setText(textResponse);
                        area.setText(textArea);


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else if (marker.equals(mSarit)) {
            reference.child("Ambulances").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        pnumber = snapshot.child("Ambulance6").child("number").getValue(String.class);
                        textTitle = snapshot.child("Ambulance6").child("title").getValue(String.class);
                        textResponse = snapshot.child("Ambulance6").child("response").getValue(String.class);
                        textArea = snapshot.child("Ambulance6").child("hospital").getValue(String.class);
                        Log.e(TAG, "onDataChange: " + pnumber);
                        Log.e(TAG, "onDataChange: " + textTitle);
                        Log.e(TAG, "onDataChange: " + textResponse);
                        title.setText(textTitle);
                        response.setText(textResponse);
                        area.setText(textArea);


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else if (marker.equals(mJacaranda)) {
            reference.child("Ambulances").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        pnumber = snapshot.child("Ambulance7").child("number").getValue(String.class);
                        textTitle = snapshot.child("Ambulance7").child("title").getValue(String.class);
                        textResponse = snapshot.child("Ambulance7").child("response").getValue(String.class);
                        textArea = snapshot.child("Ambulance7").child("hospital").getValue(String.class);
                        Log.e(TAG, "onDataChange: " + pnumber);
                        Log.e(TAG, "onDataChange: " + textTitle);
                        Log.e(TAG, "onDataChange: " + textResponse);
                        title.setText(textTitle);
                        response.setText(textResponse);
                        area.setText(textArea);


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else if (marker.equals(mMuseum)) {
            reference.child("Ambulances").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        pnumber = snapshot.child("Ambulance8").child("number").getValue(String.class);
                        textTitle = snapshot.child("Ambulance8").child("title").getValue(String.class);
                        textResponse = snapshot.child("Ambulance8").child("response").getValue(String.class);
                        textArea = snapshot.child("Ambulance8").child("hospital").getValue(String.class);
                        Log.e(TAG, "onDataChange: " + pnumber);
                        Log.e(TAG, "onDataChange: " + textTitle);
                        Log.e(TAG, "onDataChange: " + textResponse);
                        title.setText(textTitle);
                        response.setText(textResponse);
                        area.setText(textArea);


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else if (marker.equals(mUniversityWay)) {
            reference.child("Ambulances").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        pnumber = snapshot.child("Ambulance9").child("number").getValue(String.class);
                        textTitle = snapshot.child("Ambulance9").child("title").getValue(String.class);
                        textResponse = snapshot.child("Ambulance9").child("response").getValue(String.class);
                        textArea = snapshot.child("Ambulance9").child("hospital").getValue(String.class);
                        Log.e(TAG, "onDataChange: " + pnumber);
                        Log.e(TAG, "onDataChange: " + textTitle);
                        Log.e(TAG, "onDataChange: " + textResponse);
                        title.setText(textTitle);
                        response.setText(textResponse);
                        area.setText(textArea);


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }





        showDialog();
        return false;
    }

    private boolean isTelephonyEnabled() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        return telephonyManager != null && telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
    }


    private void showDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_layout);
        Button call = dialog.findViewById(R.id.call);
        title = dialog.findViewById(R.id.title);
        response = dialog.findViewById(R.id.response);
        area = dialog.findViewById(R.id.area);
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialodAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callFuntion(pnumber);
            }
        });
    }

    private void callFuntion(String num) {
        Intent call = new Intent(Intent.ACTION_DIAL);
        call.setData(Uri.parse("tel:" + num));
        startActivity(call);
    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void enableMyLocation() {
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (map != null) {
                map.setMyLocationEnabled(true);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
        // [END maps_check_location_permission]
    }

    // [START maps_check_location_permission_result]
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
            // [END_EXCLUDE]
        }
    }
    // [END maps_check_location_permission_result]

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


}