package com.college.geteat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.college.geteat.R;
import com.college.geteat.activities.MainScreenActivity;
import com.college.geteat.entity.Client;
import com.college.geteat.entity.MyLatLng;
import com.college.geteat.utils.DB_Keys;
import com.college.geteat.utils.Utils;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.Objects;


public class Fragment_DetailsForm extends Fragment {

    private Place userPlace;
    private ImageView loginDetails_IMG_edit;
    private MaterialButton loginDetails_BTN_next;
    private AppCompatImageView loginDetails_IMG_profileImage;
    private TextInputEditText loginDetails_EDT_name;
    private TextInputEditText loginDetails_EDT_address;


    public Fragment_DetailsForm() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment__details_form, container, false);

        findViews(view);

        initEditProfilePic();

        initAddressAutoComplete();

        initNextBtnListener();

        return view;
    }

    private void initEditProfilePic() {
        loginDetails_IMG_edit.setOnClickListener(v -> ImagePicker
                .create(Fragment_DetailsForm.this) // Activity or Fragment
                .limit(1)
                .showCamera(false)
                .start());
    }


    private void initNextBtnListener() {


        loginDetails_BTN_next.setOnClickListener(v -> {
            String name = Objects.requireNonNull(loginDetails_EDT_name.getText()).toString();

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "name must not be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            assert getArguments() != null;
            String uid = Fragment_DetailsFormArgs.fromBundle(getArguments()).getUid();
            Client client = new Client()
                    .setName(name)
                    .setLatLng(new MyLatLng(Objects.requireNonNull(userPlace.getLatLng())));

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String dbPath = Utils.generateFireBaseDBPath(DB_Keys.USERS, DB_Keys.CLIENTS, uid);
            DatabaseReference myRef = database.getReference(dbPath);

            myRef.setValue(client);

            Utils.startNewActivity(requireActivity(), MainScreenActivity.class);

        });

    }


    private void initAddressAutoComplete() {
        Places.initialize(requireContext().getApplicationContext(), Utils.PLACES_API_KEY);
        loginDetails_EDT_address.setFocusable(false);
        loginDetails_EDT_address.setOnClickListener(v -> {
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN
                    , Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                    .setTypeFilter(TypeFilter.ADDRESS)
                    .setCountry(Utils.ISRAEL_CODE)
                    .build(requireContext().getApplicationContext());
            startActivityForResult(intent, Utils.AUTOCOMPLETE_REQUEST_CODE);
        });
    }

    private void findViews(View view) {

        loginDetails_IMG_profileImage = view.findViewById(R.id.loginDetails_IMG_profileImage);
        loginDetails_IMG_edit = view.findViewById(R.id.loginDetails_IMG_edit);
        loginDetails_BTN_next = view.findViewById(R.id.loginDetails_BTN_next);
        loginDetails_EDT_name = view.findViewById(R.id.loginDetails_EDT_name);
        loginDetails_EDT_address = view.findViewById(R.id.loginDetails_EDT_address);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == Utils.RESULT_CANCELED) {
            return;
        }

        if (requestCode == Utils.AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Utils.RESULT_OK) {

                assert data != null;
                userPlace = Autocomplete.getPlaceFromIntent(data);
                loginDetails_EDT_address.setText(userPlace.getName());
                Log.i("pttt", "Place: " + userPlace.getName() + ", " + userPlace.getId() + " , " + userPlace.getLatLng());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                assert data != null;
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("pttt", status.getStatusMessage());
            }
            return;
        }
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // get a single image only
            Image image = ImagePicker.getFirstImageOrNull(data);
            StorageReference storageRef = FirebaseStorage.getInstance().getReference(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
            storageRef.putFile(image.getUri());
            Glide.with(this)
                    .load(image.getUri())
                    .centerCrop()
                    .into(loginDetails_IMG_profileImage);

            Log.i("pttt", "onActivityResult: " + image.getUri());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}