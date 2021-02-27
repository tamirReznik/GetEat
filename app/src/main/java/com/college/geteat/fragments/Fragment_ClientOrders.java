package com.college.geteat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.college.geteat.adapters.Adapter_Orders;
import com.college.geteat.R;
import com.college.geteat.entity.Order;
import com.college.geteat.utils.DB_Keys;
import com.college.geteat.utils.Utils;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Fragment_ClientOrders extends Fragment {
    private String uid;
    private ObservableRecyclerView clientOrders_LST_results;
    private Adapter_Orders adapter_orders;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uid = FirebaseAuth.getInstance().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment__client_orders, container, false);

        clientOrders_LST_results = view.findViewById(R.id.clientOrders_LST_results);

        clientOrders_LST_results.setLayoutManager(new LinearLayoutManager(requireContext()));


        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference(Utils.generateFireBaseDBPath(DB_Keys.ORDERS, DB_Keys.DETAILS, uid, DB_Keys.AS_CLIENT));
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    ArrayList<Order> orders = new ArrayList<>();
                    for (DataSnapshot order : snapshot.getChildren()) {
                        orders.add(order.getValue(Order.class));
                    }
                    adapter_orders = new Adapter_Orders(requireContext(), orders);
                    clientOrders_LST_results.setAdapter(adapter_orders);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;


    }
}