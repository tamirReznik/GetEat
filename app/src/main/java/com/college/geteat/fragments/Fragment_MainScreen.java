package com.college.geteat.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.college.geteat.R;
import com.college.geteat.activities.LoginActivity;
import com.college.geteat.entity.Courier;
import com.college.geteat.entity.Order;
import com.college.geteat.utils.DB_Keys;
import com.college.geteat.utils.SharedPreferencesManager;
import com.college.geteat.utils.Utils;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;


public class Fragment_MainScreen extends Fragment {
    private static final int LOCATION_REFRESH_TIME = 1000;
    private static final int LOCATION_REFRESH_DISTANCE = 2;
    private static final int REQUEST_CODE = 101;

    private boolean backPressedOnce = false;
    private SwitchMaterial main_SWT_mode;
    private ImageView main_LBL_mode;
    private ImageView main_IMG_profileImage, main_IMG_edit;

    private Courier currentCourier;
    private boolean isCourierActive = false, courierIsExist;
    private String uid;
    private LocationManager locationManager;
    private GeoFire geoFireActiveCouriersRef;
    private ValueEventListener fetchCourierProfileEventListener;

    private Dialog orderPopUp;

    public Fragment_MainScreen() {
        // Required empty public constructor
    }

    public void openCourierDetailsFragment() {

        NavHostFragment.findNavController(this).navigate(R.id.action_fragment_MainScreen_to_fragment_CourierDetails);


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uid = FirebaseAuth.getInstance().getUid();

        initLocation();
        initValueListener();
        validateCourierInDB();
        waitForOrderListener();
        initBackPressed();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_screen, container, false);


        findViews(view);

        initProfilePic();

        initEditProfilePic();

        initModeSwitch();

        Glide
                .with(this)
                .load(R.drawable.img_background_portrait)
                .centerCrop()
                .into((ImageView) view.findViewById(R.id.main_IMG_back));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        handleSwitchBtn();

        view.findViewById(R.id.main_BTN_orders).setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_fragment_MainScreen_to_ordersFragment));

        view.findViewById(R.id.main_BTN_search).setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_fragment_MainScreen_to_fragment_SearchForm));

        view.findViewById(R.id.main_BTN_signOut).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Utils.startNewActivity(requireActivity(), LoginActivity.class);
            SharedPreferencesManager.write(DB_Keys.DB_STATE, DB_Keys.STATE.UNSIGNED.name());
        });
    }


    private void handleSwitchBtn(){
        main_SWT_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isCourierActive = isChecked;

                if (courierIsExist) {
                    if (isChecked) {
                        if (!Utils.isLocationPermGranted(requireActivity())) {
                            isCourierActive = false;
                            main_SWT_mode.setChecked(false);
                            Toast.makeText(requireContext(), "Location Permissions Required", Toast.LENGTH_SHORT).show();
                        } else {
                            Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            geoFireActiveCouriersRef.setLocation(uid, new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()));
                        }
                    } else {
                        geoFireActiveCouriersRef.removeLocation(uid);
                    }

                } else {

                    geoFireActiveCouriersRef.removeLocation(uid);
                    main_SWT_mode.setChecked(false);
                    openCourierDetailsFragment();
                }

                if (isCourierActive) {
                    main_LBL_mode.setImageResource(R.drawable.active_courier);
                } else {
                    main_LBL_mode.setImageResource(R.drawable.inactive_courier);
                }

            }
        });
    }

    private void waitForOrderListener() {

        FirebaseDatabase.getInstance().getReference(Utils.generateFireBaseDBPath(DB_Keys.ORDERS, DB_Keys.PENDING_ORDERS, uid)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null && isCourierActive) {
                    Order currentOrder = snapshot.getValue(Order.class);


                    isCourierActive = false;
                    geoFireActiveCouriersRef.removeLocation(uid);
                    main_SWT_mode.setChecked(false);

                    assert currentOrder != null;
                    popOrderAlert(currentOrder);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void popOrderAlert(Order currentOrder) {
        orderPopUp = new Dialog(requireContext());
        orderPopUp.setContentView(R.layout.order_popup_layout);

        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        List<Address> pickUpAddress = null, destAddress = null;
        try {
            pickUpAddress = geocoder.getFromLocation(currentOrder.getEstablishment().getLat(), currentOrder.getEstablishment().getLng(), 1);
            destAddress = geocoder.getFromLocation(currentOrder.getTarget().getLat(), currentOrder.getTarget().getLng(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert pickUpAddress != null;
        ((TextView) orderPopUp.findViewById(R.id.order_LBL_from)).setText(format("From: %s", pickUpAddress.get(0).getAddressLine(0)));
        ((TextView) orderPopUp.findViewById(R.id.order_LBL_number)).setText(format(Locale.getDefault(), "Order Number: %s", currentOrder.getOrderNumber()));
        assert destAddress != null;
        ((TextView) orderPopUp.findViewById(R.id.order_LBL_to)).setText(format("To: %s", destAddress.get(0).getAddressLine(0)));

        MaterialButton order_BTN_getEat = orderPopUp.findViewById(R.id.order_BTN_getEat);
        String getEat = "<font color=#000000>Get</font><font color=#DA1A1A>E</font><font color=#000000>at</font>";
        order_BTN_getEat.setText(Html.fromHtml(getEat));

        DatabaseReference approvedOrderRef = FirebaseDatabase.getInstance().getReference(Utils.generateFireBaseDBPath(DB_Keys.ORDERS, DB_Keys.RESPONSE, currentOrder.getCourierID()));

        order_BTN_getEat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference(Utils.generateFireBaseDBPath(DB_Keys.ORDERS));
                GeoFire activeOrderGeoFire = new GeoFire(ordersRef.child(DB_Keys.ACTIVE_ORDERS).child(DB_Keys.TRACKER));
                GeoFire pendingOrderGeoFire = new GeoFire(ordersRef.child(DB_Keys.PENDING_ORDERS));

                //order been approved - no more pending
                pendingOrderGeoFire.removeLocation(uid);

                @SuppressLint("MissingPermission") Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                activeOrderGeoFire.setLocation(uid, new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()));
//                notify client about accept
                approvedOrderRef.setValue(true);
                orderPopUp.dismiss();

            }
        });
        orderPopUp.findViewById(R.id.order_BTN_decline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference(Utils.generateFireBaseDBPath(DB_Keys.ORDERS));
                GeoFire pendingOrderGeoFire = new GeoFire(ordersRef.child(DB_Keys.PENDING_ORDERS));
                //order been decline - no more pending
                pendingOrderGeoFire.removeLocation(uid);

                DatabaseReference orderInfoRef = FirebaseDatabase.getInstance().getReference(Utils.generateFireBaseDBPath(DB_Keys.ORDERS, DB_Keys.ACTIVE_ORDERS, DB_Keys.DETAILS));

                orderInfoRef.child(currentOrder.getClientID()).child(DB_Keys.AS_CLIENT).child(currentOrder.getDateCreated()).setValue(currentOrder);
                orderInfoRef.child(currentOrder.getCourierID()).child(DB_Keys.AS_COURIER).child(currentOrder.getDateCreated()).setValue(currentOrder);
                //notify client about decline
                approvedOrderRef.setValue(false);
                orderPopUp.dismiss();
            }
        });

        orderPopUp.setCanceledOnTouchOutside(false);
        orderPopUp.setCancelable(false);
        orderPopUp.show();
    }

    private void validateCourierInDB() {
        DatabaseReference courierProRef = FirebaseDatabase
                .getInstance()
                .getReference(Utils.generateFireBaseDBPath(DB_Keys.USERS, DB_Keys.COURIERS, DB_Keys.COURIERS_PROFILES, uid));

        courierProRef.addListenerForSingleValueEvent(fetchCourierProfileEventListener);

        DatabaseReference geoFireRef = FirebaseDatabase
                .getInstance()
                .getReference(Utils.generateFireBaseDBPath(DB_Keys.USERS, DB_Keys.COURIERS, DB_Keys.ACTIVE_COURIERS));

        geoFireActiveCouriersRef = new GeoFire(geoFireRef);

    }

    private void initValueListener() {
        fetchCourierProfileEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if ((courierIsExist = snapshot.exists())) {
                    currentCourier = snapshot.getValue(Courier.class);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("pttt", "onCancelled: " + error.getDetails());

            }
        };
    }

    private void initBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.i("pttt", "handleOnBackPressed: ");
                if (backPressedOnce) {
                    requireActivity().finish();
                } else {
                    backPressedOnce = true;
                    Toast.makeText(requireContext().getApplicationContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
                    new Handler(Looper.myLooper()).postDelayed(() -> backPressedOnce = false, 1500);
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }




    private void initModeSwitch() {
        main_LBL_mode.setOnClickListener(v -> {
            if (isCourierActive) {
                openCourierDetailsFragment();
            }
        });
    }

    private void initEditProfilePic() {
        main_IMG_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImagePicker
                        .create(Fragment_MainScreen.this) // Activity or Fragment
                        .limit(1)
                        .showCamera(false)
                        .start();
            }
        });
    }

    private void initProfilePic() {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference(uid);
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (uri != null) {
                    Glide.with(requireContext()).load(uri).centerCrop().into(main_IMG_profileImage);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("pttt", "onFailure: " + e.getMessage());
            }
        });

    }

    private void findViews(View view) {
        main_IMG_profileImage = view.findViewById(R.id.main_IMG_profileImage);
        main_IMG_edit = view.findViewById(R.id.main_IMG_edit);

        main_SWT_mode = view.findViewById(R.id.main_SWT_mode);
        main_LBL_mode = view.findViewById(R.id.main_LBL_mode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // get a single image only
            Image image = ImagePicker.getFirstImageOrNull(data);
            StorageReference storageRef = FirebaseStorage.getInstance().getReference(uid);
            storageRef.putFile(image.getUri());
            Glide.with(this)
                    .load(image.getUri())
                    .centerCrop()
                    .into(main_IMG_profileImage);
        }

    }

    public void initLocation() {
        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!Utils.checkLocationPermission(requireActivity(), REQUEST_CODE)) {
            Log.i("pttt", "initLocation: ");
            myLocationListener();
        }

    }

    @SuppressLint("MissingPermission")
    private void myLocationListener() {

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, new LocationListener() {

                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        if (isCourierActive) {
                            //write to geofire
                            geoFireActiveCouriersRef.setLocation(uid, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            Log.i("pttt", "onLocationChanged: " + location.getLatitude() + " lon: " + location.getLongitude());
                        }
//
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

}