package com.college.geteat.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.college.geteat.R;
import com.college.geteat.entity.Client;
import com.college.geteat.entity.MyLatLng;
import com.college.geteat.entity.Order;
import com.college.geteat.utils.DB_Keys;
import com.college.geteat.utils.Utils;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Fragment_SearchForm extends Fragment {

    DatePickerDialog.OnDateSetListener setListener;
    private TextInputEditText search_EDT_address;
    private TextInputEditText search_EDT_order;
    private TextView search_EDT_date;
    private boolean isForTodayOrder, isDateTimeValid;
    private Place pickUpPlace;
    private String orderNumber;
    private String uid;
    private Calendar pickUpDate;
    ValueEventListener clientFetchEventListener;
    int timePicker_hour, timePicker_minute;
    Fragment currentFragment;


    public Fragment_SearchForm() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        currentFragment = this;
        super.onCreate(savedInstanceState);
        clientFetchEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Log.i("pttt", "onDataChange: " + snapshot);
                if (snapshot.getValue() != null) {
                    Client client = snapshot.getValue(Client.class);

                    Order order = new Order(uid, orderNumber, new MyLatLng(pickUpPlace.getLatLng()), client.getLatLng());
                    navigateToSearchResultsFragment(order);

                    isDateTimeValid = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("pttt", "onDataChange: " + error.getDetails());
            }
        };


    }

    public void navigateToSearchResultsFragment(Order order) {

        Log.i("pttt", "navigateToSearchResultsFragment: " + order);

        Fragment_SearchFormDirections.ActionFragmentSearchFormToFragmentSearchResults action =
                Fragment_SearchFormDirections.actionFragmentSearchFormToFragmentSearchResults(order);
        NavHostFragment.findNavController(this).navigate(action);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_form, container, false);

        findViews(view);
        initAddressAutoComplete();
        initDatePicker(view);
        initClockPicker(view);

        initNextBTN(view);

        return view;
    }

    public void fetchClient() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(requireContext(), "Authentication error!", Toast.LENGTH_SHORT).show();
            return;
        }

        uid = user.getUid();

        String dbPath = Utils.generateFireBaseDBPath(DB_Keys.USERS, DB_Keys.CLIENTS, uid);

        FirebaseDatabase.getInstance().getReference(dbPath).addListenerForSingleValueEvent(clientFetchEventListener);
    }

    private void initNextBTN(View view) {
        view.findViewById(R.id.search_BTN_search).setOnClickListener(v -> {

            if (isInputValid()) {
                fetchClient();
                isDateTimeValid = false;
            }
        });
    }

    private boolean isInputValid() {
        if (!isDateTimeValid) {
            Toast.makeText(requireContext(), "Must Pick Date And Time First", Toast.LENGTH_LONG).show();
            return false;
        }

        if (pickUpPlace == null) {
            Toast.makeText(requireContext(), "Must Pick Place First", Toast.LENGTH_LONG).show();
            return false;
        }

        if (search_EDT_order.getText() == null) {
            Toast.makeText(requireContext(), "Must Enter Order Number First", Toast.LENGTH_LONG).show();
            return false;
        }

        orderNumber = search_EDT_order.getText().toString();

        if (orderNumber.equals(getResources().getString(R.string.order_number)) || orderNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Must Enter Order Number First", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void findViews(View view) {

        search_EDT_order = view.findViewById(R.id.search_EDT_order);
        search_EDT_address = view.findViewById(R.id.search_EDT_address);
    }

    private void initAddressAutoComplete() {
        Places.initialize(requireContext().getApplicationContext(), Utils.PLACES_API_KEY);

        search_EDT_address.setFocusable(false);

        search_EDT_address.setOnClickListener(v -> {
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN
                    , Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS))
                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                    .setCountry(Utils.ISRAEL_CODE)
                    .build(requireContext().getApplicationContext());
            startActivityForResult(intent, Utils.AUTOCOMPLETE_REQUEST_CODE);
        });
    }

    private void placeAutoCompleteResult(int requestCode, int resultCode, @Nullable Intent data) {
        int RESULT_OK = -1;
        int RESULT_CANCELED = 0;
        assert data != null;
        if (resultCode == RESULT_OK) {


            pickUpPlace = Autocomplete.getPlaceFromIntent(data);
            validateAddressInput(pickUpPlace);


        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            // TODO: Handle the error.
            Status status = Autocomplete.getStatusFromIntent(data);
            Log.i("pttt", status.getStatusMessage());
        } else if (resultCode == RESULT_CANCELED) {
            // The user canceled the operation.
            Log.i("pttt", "autoCompleteResult: RESULT_CANCELED");
        }

    }

    private void validateAddressInput(Place pickUpPlace) {
        if (pickUpPlace == null) {
            return;
        }

        Log.i("pttt", "validateAddressInput: " + pickUpPlace.getAddress());

        search_EDT_address.setText(pickUpPlace.getName());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        assert data != null;
        if (requestCode == Utils.AUTOCOMPLETE_REQUEST_CODE) {
            placeAutoCompleteResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void initClockPicker(View view) {

        TextView search_EDT_time = view.findViewById(R.id.search_EDT_time);
        search_EDT_time.setOnClickListener(v -> {
            //Initialize time picker dialog
            if (this.search_EDT_date.getText().equals(getResources().getString(R.string.pick_up_date))) {
                Toast.makeText(requireContext(), "Pick Date First", Toast.LENGTH_SHORT).show();
                return;
            }
            TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(),
                    R.style.DateAndTimePickerTheme,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view1, int hourOfDay, int minute) {

                            Calendar minDate = Calendar.getInstance();
                            minDate.add(Calendar.MINUTE, 15);
                            Log.i("pttt", "currenttime : " + minDate.getTime().toString());
                            timePicker_hour = hourOfDay;
                            timePicker_minute = minute;

                            Calendar date1 = Calendar.getInstance();
                            date1.set(Calendar.MINUTE, minute);
                            date1.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            // Initialize hour and minute
                            java.lang.String time = hourOfDay + ":" + minute;
                            Log.i("pttt", "onTimeSet: " + time);

                            SimpleDateFormat format_24h = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            try {

                                Date date = format_24h.parse(time);
                                SimpleDateFormat format_12h = new SimpleDateFormat("HH:mm aa", Locale.getDefault());


                                if (isForTodayOrder) {
                                    if (date1.getTime().before(minDate.getTime())) {
                                        Toast.makeText(requireContext(), "Invalid Time - set order to at least 15 minutes from now", Toast.LENGTH_LONG).show();
                                        isDateTimeValid = false;
                                        return;
                                    }
                                }

                                isDateTimeValid = true;
                                pickUpDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                pickUpDate.set(Calendar.MINUTE, minute);
                                search_EDT_time.setText(format_12h.format(date));
                                Log.i("time", "onTimeSet: " + format_12h.format(date));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 12, 0, false);
            timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

            timePickerDialog.updateTime(timePicker_hour, timePicker_minute);
            timePickerDialog.show();
        });
    }

    public void initDatePicker(View view) {
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        search_EDT_date = view.findViewById(R.id.search_LBL_date);
        search_EDT_date.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(),
                    R.style.DateAndTimePickerTheme, setListener, year, month, day);
            //datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            Calendar c = Calendar.getInstance();
            c.setTime(new Date()); // Now use today date.
            datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());

            c.add(Calendar.DATE, 7); // Adding 7 days
            datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
            datePickerDialog.show();
        });

        setListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {


                isForTodayOrder = dayOfMonth == Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

                pickUpDate = Calendar.getInstance();
                pickUpDate.set(year, month, dayOfMonth);

                String date = dayOfMonth + "/" + ++month + "/" + year;
                search_EDT_date.setText(date);


            }
        };
    }
}