package com.college.geteat.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Courier implements Parcelable {

    private float rank = 0;
    private int rankCounter = 0;
    private double radius = 0;
    private boolean isActive;
    private int orderCounter = 0;
    private String name = "";
    private MyLatLng latLng;
    private String uid = "";


    public Courier(int rank, double radius, boolean isActive, int orderCounter, String name, String uid, MyLatLng latLng) {
        this.rank = rank;
        this.radius = radius;
        this.isActive = isActive;
        this.orderCounter = orderCounter;
        this.name = name;
        this.uid = uid;
        this.latLng = latLng;
    }

    public Courier() {
    }

    protected Courier(Parcel in) {
        rank = in.readFloat();
        rankCounter = in.readInt();
        radius = in.readDouble();
        isActive = in.readByte() != 0;
        orderCounter = in.readInt();
        name = in.readString();
        latLng = in.readParcelable(MyLatLng.class.getClassLoader());
        uid = in.readString();
    }

    public static final Creator<Courier> CREATOR = new Creator<Courier>() {
        @Override
        public Courier createFromParcel(Parcel in) {
            return new Courier(in);
        }

        @Override
        public Courier[] newArray(int size) {
            return new Courier[size];
        }
    };

    public float getRank() {
        return rank;
    }

    public Courier setRank(float rank) {
        this.rank = rank;
        return this;
    }

    public int getOrderCounter() {
        return orderCounter;
    }

    public Courier setOrderCounter(int orderCounter) {
        this.orderCounter = orderCounter;
        return this;
    }

    public double getRadius() {
        return radius;
    }

    public String getName() {
        return name;
    }

    public Courier setName(String name) {
        this.name = name;
        return this;
    }

    public String getUid() {
        return uid;
    }

    public Courier setUid(String uid) {
        this.uid = uid;
        return this;
    }


    public Courier setRadius(double radius) {
        this.radius = radius;
        return this;
    }

    public boolean isActive() {
        return isActive;
    }

    public Courier setActive(boolean active) {
        isActive = active;
        return this;
    }


    public int getRankCounter() {
        return rankCounter;
    }

    public Courier setRankCounter(int rankCounter) {
        this.rankCounter = rankCounter;
        return this;
    }


    public MyLatLng getLatLng() {
        return latLng;
    }

    public Courier setLatLng(MyLatLng latLng) {
        this.latLng = latLng;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(rank);
        dest.writeInt(rankCounter);
        dest.writeDouble(radius);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeInt(orderCounter);
        dest.writeString(name);
        dest.writeParcelable(latLng, flags);
        dest.writeString(uid);
    }
}
