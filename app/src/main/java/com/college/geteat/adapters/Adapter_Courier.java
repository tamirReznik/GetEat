package com.college.geteat.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.college.geteat.R;
import com.college.geteat.entity.Courier;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Adapter_Courier extends RecyclerView.Adapter<Adapter_Courier.CourierViewHolder> {

    private ArrayList<Courier> couriers;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private RatingBar ratingBar;


    // data is passed into the constructor
    public Adapter_Courier(Context context, ArrayList<Courier> data) {
        this.mInflater = LayoutInflater.from(context);
        this.couriers = data;

    }

    // data is passed into the constructor
    public Adapter_Courier(Context context) {
        this.mInflater = LayoutInflater.from(context);
        couriers = new ArrayList<>();
    }

    public void addAll(List<Courier> newCouriers) {
        int initSize = couriers.size();
        couriers.addAll(newCouriers);
        notifyItemRangeChanged(initSize, newCouriers.size());
    }


    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public CourierViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_courier, parent, false);
        return new CourierViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(CourierViewHolder holder, int position) {
        Log.d("pttt", "Binding " + position);
        Courier courier = couriers.get(position);
        holder.courierTab_LBL_name.setText(courier.getName());
        holder.courierTab_LBL_counter.setText(String.format(Locale.getDefault(), "%d Orders So Far", courier.getOrderCounter()));
        holder.courierTab_RTB_rank.setRating(courier.getRank());

        StorageReference storageRef = FirebaseStorage.getInstance().getReference(courier.getUid());
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            if (uri != null) {
                Glide.with(holder.itemView.getContext()).load(uri).centerCrop().into(holder.courierTab_IMG_user);
            }
        }).addOnFailureListener(e -> Log.i("pttt", "onFailure: " + e.getMessage()));

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return couriers.size();
    }

    // convenience method for getting data at click position
    public Courier getItem(int id) {
        return couriers.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);

        void onReportClick(int position);
    }

    // stores and recycles views as they are scrolled off screen
    public class CourierViewHolder extends RecyclerView.ViewHolder {

        ImageView courierTab_IMG_user;
        TextView courierTab_LBL_name;
        TextView courierTab_LBL_counter;
        RatingBar courierTab_RTB_rank;

        CourierViewHolder(View itemView) {
            super(itemView);
            courierTab_IMG_user = itemView.findViewById(R.id.courierTab_IMG_profile);
            courierTab_LBL_name = itemView.findViewById(R.id.courierTab_LBL_name);
            courierTab_LBL_counter = itemView.findViewById(R.id.courierTab_LBL_counter);
            courierTab_RTB_rank = itemView.findViewById(R.id.courierTab_RTB_rank);

            itemView.setOnClickListener(v -> {
                if (mClickListener != null) {
                    mClickListener.onItemClick(v, getAdapterPosition());
                }
            });
        }
    }
}

