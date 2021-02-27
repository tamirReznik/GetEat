package com.college.geteat.activities;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.college.geteat.R;
import com.college.geteat.utils.DB_Keys;
import com.college.geteat.utils.Utils;
import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainScreenActivity extends AppCompatActivity {
    private LocationManager locationManager;
    private GeoFire geoFire;
    private static final int LOCATION_REFRESH_TIME = 1000;
    private static final int LOCATION_REFRESH_DISTANCE = 2;
    private static final int REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        initLocation();

    }

    private void initLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!Utils.checkLocationPermission(this, REQUEST_CODE)) {
            myLocationListener();
        }
    }


    @SuppressLint("MissingPermission")
    private void myLocationListener() {

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
//                        SharedPreferencesManager.write(SharedPreferencesManager.LAT, Double.toString(location.getLatitude()));
//                        SharedPreferencesManager.write(SharedPreferencesManager.LON, Double.toString(location.getLongitude()));
                        Log.i("onLocationChan", "onLocationChanged: " + location.getLatitude() + " lon: " + location.getLongitude());
                    }

                    @Override
                    public void onProviderEnabled(@NonNull String provider) {

                    }

                    @Override
                    public void onProviderDisabled(@NonNull String provider) {

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0)
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    myLocationListener();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference(Utils.generateFireBaseDBPath(DB_Keys.USERS, DB_Keys.COURIERS, DB_Keys.ACTIVE_COURIERS));

            ref.child(Objects.requireNonNull(Utils.uid)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        geoFire = new GeoFire(ref);
                        geoFire.removeLocation(FirebaseAuth.getInstance().getUid());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });




    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}