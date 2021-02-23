package com.college.geteat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.college.geteat.R;
import com.college.geteat.entity.Courier;
import com.college.geteat.utils.DB_Keys;
import com.college.geteat.utils.Utils;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.Objects;


public class Fragment_CourierDetails extends Fragment {

    private TextInputEditText courierDetails_EDT_address, courierDetails_EDT_name, courierDetails_EDT_radius;
    private MaterialButton courierDetails_BTN_next;
    private Place courierPlace;
    private String courierName;

    private double radius;

    public Fragment_CourierDetails() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment__courier_details, container, false);

        findViews(view);
        initAddressAutoComplete();
        initNextBtn();

        return view;
    }

    private void initNextBtn() {
        courierDetails_BTN_next.setOnClickListener(v -> {
            if (allFieldsAreValid()) {
                String uid = FirebaseAuth.getInstance().getUid();

                Courier courier = new Courier()
                        .setAddress(courierPlace.getAddress())
                        .setName(courierName)
                        .setRadius(radius)
                        .setUid(uid)
                        .setRank(0)
                        .setOrderCounter(0);


                DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Utils.generateFireBaseDBPath(DB_Keys.USERS, DB_Keys.COURIERS));
                ref.child(DB_Keys.COURIERS_PROFILES).child(uid).setValue(courier);

                Fragment_CourierDetailsDirections.ActionFragmentCourierDetailsToFragmentMainScreen action
                        = Fragment_CourierDetailsDirections.actionFragmentCourierDetailsToFragmentMainScreen(DB_Keys.SUCCESS);
//
                NavHostFragment.findNavController(Fragment_CourierDetails.this).navigate(action);
            }
        });
    }

    private boolean allFieldsAreValid() {
        if (courierPlace == null) {
            Toast.makeText(requireContext(), "Please Pick Center Address", Toast.LENGTH_LONG).show();
            return false;
        }

        radius = Double.parseDouble(Objects.requireNonNull(courierDetails_EDT_radius.getText()).toString());

        if (radius > 4 || radius < 0.4) {
            Toast.makeText(requireContext(), "Radius Must Be Between 0.4 And 4.0", Toast.LENGTH_LONG).show();
            courierDetails_EDT_radius.setText("");
            return false;
        }

        courierName = Objects.requireNonNull(courierDetails_EDT_name.getText()).toString();

        if (courierName.isEmpty()) {
            Toast.makeText(requireContext(), "Please Enter Your Name", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;

    }

    private void findViews(View view) {
        courierDetails_EDT_address = view.findViewById(R.id.courierDetails_EDT_address);
        courierDetails_EDT_name = view.findViewById(R.id.courierDetails_EDT_name);
        courierDetails_EDT_radius = view.findViewById(R.id.courierDetails_EDT_radius);
        courierDetails_BTN_next = view.findViewById(R.id.courierDetails_BTN_next);
    }

    private void initAddressAutoComplete() {
        Places.initialize(requireContext().getApplicationContext(), Utils.PLACES_API_KEY);
        courierDetails_EDT_address.setFocusable(false);
        courierDetails_EDT_address.setOnClickListener(v -> {
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN
                    , Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                    .setTypeFilter(TypeFilter.ADDRESS)
                    .setCountry(Utils.ISRAEL_CODE)
                    .build(requireContext().getApplicationContext());
            startActivityForResult(intent, Utils.AUTOCOMPLETE_REQUEST_CODE);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        assert data != null;
        if (requestCode == Utils.AUTOCOMPLETE_REQUEST_CODE) {

            if (resultCode == Utils.RESULT_OK) {

                courierPlace = Autocomplete.getPlaceFromIntent(data);
                courierDetails_EDT_address.setText(courierPlace.getName());
                Log.i("pttt", "Place: " + courierPlace.getName() + ", " + courierPlace.getId() + " , " + courierPlace.getLatLng());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("pttt", status.getStatusMessage());
            } else if (resultCode == Utils.RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }


        super.onActivityResult(requestCode, resultCode, data);
    }


}