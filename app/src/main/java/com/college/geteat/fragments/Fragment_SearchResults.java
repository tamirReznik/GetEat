package com.college.geteat.fragments;

import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.college.geteat.Adapter_Courier;
import com.college.geteat.R;
import com.college.geteat.activities.MainScreenActivity;
import com.college.geteat.entity.Courier;
import com.college.geteat.entity.MyLatLng;
import com.college.geteat.entity.Order;
import com.college.geteat.utils.DB_Keys;
import com.college.geteat.utils.Utils;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;


public class Fragment_SearchResults extends Fragment {

//    final int ITEM_LOAD_COUNT = 5;
//    int total_item = 0, last_visible_item;
//    boolean isLoading = false, isMaxData = false;
//    String last_node = "", last_key = "";

    private ObservableRecyclerView main_LST_names;
    private ActionBar actionBar;
    private Adapter_Courier adapter_courier;
    private Order order;
    private double radius;
    private ArrayList<Courier> couriers;
    private HashSet<String> couriersKeys;
    private ValueEventListener fetchCourierProfile;
    private String uid;
    int numOfCouriersLastFetch;
    private ValueEventListener orderApprovedListener;
    ObservableRecyclerView recyclerView;

    ObservableScrollViewCallbacks observableScrollViewCallbacks;


    public Fragment_SearchResults() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        order = Fragment_SearchResultsArgs.fromBundle(getArguments()).getOrder();
        Log.i("pttt", "onCreate: " + order.toString());
        uid = FirebaseAuth.getInstance().getUid();

        initFetchVars();

        initFetchCouriersProfile();

        fetchCouriers();


    }

    public void initFetchCouriersProfile() {
        fetchCourierProfile = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("pttt", "onDataChange: ");
                if (snapshot != null) {

                    Courier courier = snapshot.getValue(Courier.class);
                    Log.i("pttt", "onDataChange: " + courier.getName());
                    if (!couriers.contains(courier)) {
                        Log.i("pttt", "onDataChange: " + couriers.size());
                        couriers.add(courier);
                    }
                    if (couriers.size() == numOfCouriersLastFetch) {
                        adapter_courier.addAll(couriers);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    private void initFetchVars() {

        couriersKeys = new HashSet<>();
        couriers = new ArrayList<>();
        radius = 50;
    }

    private void fetchCouriers() {

        DatabaseReference couriersRef = FirebaseDatabase.getInstance()
                .getReference(Utils.generateFireBaseDBPath(DB_Keys.USERS, DB_Keys.COURIERS));
        GeoFire geoFire = new GeoFire(couriersRef.child(DB_Keys.ACTIVE_COURIERS));

        MyLatLng latLng = order.getEstablishment();
        Log.i("pttt", "fetchCouriers: " + latLng.toString());

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.getLat(), latLng.getLng()), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.i("'pttt'", "onKeyEntered: " + key);
                if (!key.equals(uid)) {
                    couriersKeys.add(key);
                }
            }

            @Override
            public void onKeyExited(String key) {
                Log.i("pttt", "onKeyExited: " + key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.i("pttt", "onKeyMoved: " + key);
            }

            @Override
            public void onGeoQueryReady() {
                Log.i("pttt", "onGeoQueryReady: " + couriersKeys.size());
                numOfCouriersLastFetch = couriersKeys.size();
                for (String courierKey : couriersKeys) {
                    couriersRef.child(DB_Keys.COURIERS_PROFILES).child(courierKey).addListenerForSingleValueEvent(fetchCourierProfile);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);

        findViews(view);

        //init recycleview
//        couriers.addAll(MockDB.generatePosts());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        main_LST_names.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(main_LST_names.getContext(), linearLayoutManager.getOrientation());
        main_LST_names.addItemDecoration(dividerItemDecoration);

        adapter_courier = new Adapter_Courier(requireContext());
        adapter_courier.setClickListener(new Adapter_Courier.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Courier chosenC = adapter_courier.getItem(position);
                order.setCourierID(chosenC.getUid());
                order.setClientID(uid);
                order.setDateCreated(new Date().toString());
                DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference(Utils.generateFireBaseDBPath(DB_Keys.ORDERS, DB_Keys.PENDING_ORDERS, chosenC.getUid()));
                Log.i("pttt", "onItemClick: " + order.toString());
                orderRef.setValue(order);
                DatabaseReference approvedOrderRef = FirebaseDatabase.getInstance().getReference(Utils.generateFireBaseDBPath(DB_Keys.ORDERS, DB_Keys.RESPONSE, chosenC.getUid()));
                initOrderApprovedListener(approvedOrderRef, order);
                approvedOrderRef.addValueEventListener(orderApprovedListener);


            }

            @Override
            public void onReportClick(int position) {

            }
        });
        main_LST_names.setAdapter(adapter_courier);

//        main_LST_names.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                total_item = linearLayoutManager.getItemCount();
//
//                last_visible_item = linearLayoutManager.findLastCompletelyVisibleItemPosition();
//
//                Log.i("pttt", "onScrolled: " + total_item + " LAST VISIBLKE: " + last_visible_item + " loading" + isLoading);
//                if (!isLoading && total_item <= (last_visible_item + ITEM_LOAD_COUNT)) {
////                    getCouriers();
//                    isLoading = true;
//                }
//            }
//        });

        //init ObservableRecyclerView - floating action bar
        if (actionBar == null) {
            setActionBar(((MainScreenActivity) requireActivity()).getSupportActionBar());
        }

        main_LST_names.setScrollViewCallbacks(observableScrollViewCallbacks);

        return view;
    }

    private void initOrderApprovedListener(DatabaseReference approvedOrderRef, Order curOrder) {
        orderApprovedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean isApproved = snapshot.getValue(Boolean.class);
                    Dialog courierResponseDialog = new Dialog(requireContext());
                    courierResponseDialog.setContentView(R.layout.order_popup_layout);


                    Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                    List<Address> pickUpAddress = null, destAddress = null;
                    try {
                        pickUpAddress = geocoder.getFromLocation(curOrder.getEstablishment().getLat(), curOrder.getEstablishment().getLng(), 1);
                        destAddress = geocoder.getFromLocation(curOrder.getTarget().getLat(), curOrder.getTarget().getLng(), 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    assert pickUpAddress != null;
                    ((TextView) courierResponseDialog.findViewById(R.id.order_LBL_from)).setText(format("From: %s", pickUpAddress.get(0).getAddressLine(0)));
                    ((TextView) courierResponseDialog.findViewById(R.id.order_LBL_number)).setText(format(Locale.getDefault(), "Order Number: %s", curOrder.getOrderNumber()));
                    assert destAddress != null;
                    ((TextView) courierResponseDialog.findViewById(R.id.order_LBL_to)).setText(format("To: %s", destAddress.get(0).getAddressLine(0)));

                    ((MaterialButton) courierResponseDialog.findViewById(R.id.order_BTN_decline)).setVisibility(View.GONE);
                    ((MaterialButton) courierResponseDialog.findViewById(R.id.order_BTN_getEat)).setVisibility(View.GONE);

                    MaterialButton order_BTN_ok = courierResponseDialog.findViewById(R.id.order_BTN_ok);
                    order_BTN_ok.setText(R.string.ok);
                    order_BTN_ok.setVisibility(View.VISIBLE);
                    order_BTN_ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            courierResponseDialog.dismiss();
                        }
                    });

                    int response = isApproved ? R.string.order_approved : R.string.order_declined;
                    int color = isApproved ? R.color.lightGreen : R.color.red_200;

                    ((TextView) courierResponseDialog.findViewById(R.id.order_LBL_headline)).setText(response);
                    ((TextView) courierResponseDialog.findViewById(R.id.order_LBL_headline)).setTextColor(ContextCompat.getColor(requireContext(), color));

                    courierResponseDialog.show();

                    approvedOrderRef.removeEventListener(this);
                    approvedOrderRef.removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    private void findViews(View view) {
        main_LST_names = view.findViewById(R.id.searchResult_LST_results);

    }

    public void setActionBar(ActionBar actionBar) {
        this.actionBar = actionBar;
        //needed only when actionBar is not null
        initCallbacks();
    }

    public void initCallbacks() {
        observableScrollViewCallbacks = new ObservableScrollViewCallbacks() {
            @Override
            public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

            }

            @Override
            public void onDownMotionEvent() {

            }

            @Override
            public void onUpOrCancelMotionEvent(ScrollState scrollState) {
                setActionBar(((MainScreenActivity) requireActivity()).getSupportActionBar());
                if (scrollState == ScrollState.UP) {
                    if (actionBar.isShowing()) {
                        actionBar.hide();
                    }
                } else if (scrollState == ScrollState.DOWN) {
                    if (!actionBar.isShowing()) {
                        actionBar.show();
                    }
                }

            }
        };
    }
}