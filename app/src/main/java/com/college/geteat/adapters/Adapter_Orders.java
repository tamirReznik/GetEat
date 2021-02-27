package com.college.geteat.adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.college.geteat.R;
import com.college.geteat.entity.Courier;
import com.college.geteat.entity.Order;
import com.college.geteat.utils.DB_Keys;
import com.college.geteat.utils.Utils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Adapter_Orders extends RecyclerView.Adapter<Adapter_Orders.OrderViewHolder> {

    private ArrayList<Order> orders;
    private LayoutInflater mInflater;
    private Adapter_Courier.ItemClickListener mClickListener;
    private RatingBar ratingBar;
    private DatabaseReference courierRef;
    private ArrayList<Boolean> hasRanked;

    boolean lock = false;

    // data is passed into the constructor
    public Adapter_Orders(Context context, List<Order> data) {
        this.mInflater = LayoutInflater.from(context);
        this.orders = new ArrayList<>(data);
        courierRef = FirebaseDatabase.getInstance().getReference(Utils.generateFireBaseDBPath(DB_Keys.USERS, DB_Keys.COURIERS, DB_Keys.COURIERS_PROFILES));
        hasRanked = new ArrayList<>();
        hasRanked.addAll(Stream.generate(() -> Boolean.FALSE).limit(data.size()).collect(Collectors.toList()));

    }


    public void addAll(List<Order> newOrders) {
        int initSize = orders.size();
        orders.addAll(newOrders);
        hasRanked.addAll(Stream.generate(() -> Boolean.FALSE).limit(newOrders.size()).collect(Collectors.toList()));
        notifyItemRangeChanged(initSize, newOrders.size());
    }


    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_orders, parent, false);
        return new OrderViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {


        Order order = orders.get(position);

        courierRef.child(order.getCourierID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    Courier currentCourier = snapshot.getValue(Courier.class);

                    StorageReference storageRef = FirebaseStorage.getInstance().getReference(order.getCourierID());

                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        if (uri != null) {
                            Glide.with(holder.itemView.getContext()).load(uri).centerCrop().into(holder.orderTab_IMG_profile);
                        }
                    }).addOnFailureListener(e -> Log.i("pttt", "onFailure: " + e.getMessage()));

                    holder.orderTab_LBL_name.setText(order.getCourierName());
                    assert currentCourier != null;
                    holder.orderTab_LBL_counter.setText(String.format(Locale.getDefault(), "%d", currentCourier.getRankCounter()));
                    holder.orderTab_LBL_rating.setText(String.format(Locale.getDefault(), "(%.1f)", currentCourier.getRank()));
                    holder.orderTab_RTB_rank.setRating(currentCourier.getRank());
                    holder.orderTab_LBL_date.setText(String.format(Locale.getDefault(), "%s", order.getDateCreated().substring(0, order.getDateCreated().indexOf('G'))));


                    Geocoder geocoder = new Geocoder(holder.itemView.getContext(), Locale.getDefault());
                    List<Address> pickUpAddress = null, destAddress = null;
                    try {
                        pickUpAddress = geocoder.getFromLocation(order.getEstablishment().getLat(), order.getEstablishment().getLng(), 1);
                        destAddress = geocoder.getFromLocation(order.getTarget().getLat(), order.getTarget().getLng(), 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    holder.orderTab_LBL_number.setText(String.format("Order Number: %s", order.getOrderNumber()));
                    assert pickUpAddress != null;
                    holder.orderTab_LBL_from.setText(String.format("From: %s", pickUpAddress.get(0).getAddressLine(0)));
                    assert destAddress != null;
                    holder.orderTab_LBL_to.setText(String.format("To: %s", destAddress.get(0).getAddressLine(0)));

                    if (!orders.get(position).isHasRated() && !hasRanked.get(position)) {
                        holder.orderTab_RTB_rank.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                            @Override
                            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                                if (!lock) {
                                    lock = true;

                                    currentCourier.setRankCounter(currentCourier.getRankCounter() + 1);
                                    float newRating = (currentCourier.getRank() + rating) / (currentCourier.getRankCounter());
                                    Log.i("pttt", "onRatingChanged: getRank" + currentCourier.getRank() + "rating " + rating + "getRankCounter " + currentCourier.getRankCounter() + rating + "newRating " + newRating);
                                    currentCourier.setRank(newRating);
                                    holder.orderTab_RTB_rank.setRating(newRating);
                                    holder.orderTab_RTB_rank.setIsIndicator(true);
                                    hasRanked.set(position, true);
                                    holder.orderTab_LBL_counter.setText(String.format(Locale.getDefault(), "(%d)", currentCourier.getRankCounter()));
                                    holder.orderTab_LBL_rating.setText(String.format(Locale.getDefault(), "(%.1f)", currentCourier.getRank()));
                                    //update rating of courier
                                    FirebaseDatabase.getInstance().getReference(Utils.generateFireBaseDBPath(DB_Keys.USERS, DB_Keys.COURIERS, DB_Keys.COURIERS_PROFILES))
                                            .child(currentCourier.getUid()).setValue(currentCourier);

                                    DatabaseReference orderInfoRef = FirebaseDatabase.getInstance().getReference(Utils.generateFireBaseDBPath(DB_Keys.ORDERS, DB_Keys.DETAILS));
                                    order.setHasRated(true);
                                    orderInfoRef.child(order.getClientID()).child(DB_Keys.AS_CLIENT).child(order.getDateCreated()).setValue(order);
                                    holder.orderTab_RTB_rank.setOnRatingBarChangeListener(null);
                                    hasRanked.set(position, true);
                                    holder.orderTab_RTB_rank.setIsIndicator(true);
                                    lock = false;
                                }
                            }
                        });
                    } else {
                        holder.orderTab_RTB_rank.setIsIndicator(true);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    // convenience method for getting data at click position
    public Order getItem(int index) {
        return orders.get(index);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {


        TextView orderTab_LBL_name, orderTab_LBL_counter, orderTab_LBL_number, orderTab_LBL_from, orderTab_LBL_to, orderTab_LBL_rating, orderTab_LBL_date;
        RatingBar orderTab_RTB_rank;
        MaterialButton orderTab_BTN_report;
        ShapeableImageView orderTab_IMG_profile;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderTab_LBL_rating = itemView.findViewById(R.id.orderTab_LBL_rating);
            orderTab_IMG_profile = itemView.findViewById(R.id.orderTab_IMG_profile);
            orderTab_LBL_name = itemView.findViewById(R.id.orderTab_LBL_name);
            orderTab_LBL_counter = itemView.findViewById(R.id.orderTab_LBL_counter);
            orderTab_LBL_number = itemView.findViewById(R.id.orderTab_LBL_number);
            orderTab_LBL_from = itemView.findViewById(R.id.orderTab_LBL_from);
            orderTab_LBL_to = itemView.findViewById(R.id.orderTab_LBL_to);
            orderTab_RTB_rank = itemView.findViewById(R.id.orderTab_RTB_rank);
            orderTab_BTN_report = itemView.findViewById(R.id.orderTab_BTN_report);
            orderTab_LBL_date = itemView.findViewById(R.id.orderTab_LBL_date);


            orderTab_RTB_rank.setIsIndicator(false);


        }
    }
}
